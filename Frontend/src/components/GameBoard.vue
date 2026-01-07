<script setup lang="ts">
import { onMounted, watch, computed, ref } from 'vue'
import Deck from './Deck.vue'
import UsedCardPile from './UsedCardPile.vue'
import PlayerPanel from './PlayerPanel.vue'
import TurnTransition from './TurnTransition.vue'
import GameOverOverlay from './GameOverOverlay.vue'
import { useOneCardGame } from '../api/useOneCardGame'
import type { CardInfo } from '../types/card'

const { gameState, setupGame, playAction, executeAITurn, fetchGameState, lastEvents } = useOneCardGame()

// Turn transition state
const showTurnTransition = ref(false)
const transitionPlayerName = ref('PLAYER')
const isBattleSituation = ref(false)
const battleCardsToDraw = ref(0)
const lastProcessedEventId = ref<string | null>(null)
const faceCardEffectActive = ref(false) // Track if player kept turn via face card
const gameOverWinner = ref<string | null>(null) // Track winner for game over animation

// NumSevenCard shape selection state
const showShapeSelection = ref(false)
const pendingNumSevenCardIndex = ref<number | null>(null)
const shapeSelectionCard = ref<CardInfo | null>(null)

// Setup game on mount
onMounted(async () => {
    await setupGame()
})

// Track if we're currently processing an AI turn to prevent loops
const isProcessingAITurn = ref(false)
const previousPlayerTurn = ref<boolean | null>(null)

// Animation state for AI actions
const animatingCard = ref<{
    card: CardInfo | null
    type: 'play' | 'draw' | null
    from: { x: number, y: number } | null
    to: { x: number, y: number } | null
    isAnimating: boolean
}>({
    card: null,
    type: null,
    from: null,
    to: null,
    isAnimating: false
})

// Track previous state to detect changes
const previousAiHandSize = ref<number>(0)
const previousLastUsedCardId = ref<number | null>(null)

// Watch for backend events to trigger animations (after transition overlay disappears)
watch(() => lastEvents.value, (events) => {
    if (!events || !gameState.value) return
    
    // If game is over, we ONLY allow processing if there's a GAME_OVER event
    // Otherwise we return to avoid side effects during game over screen
    if (gameState.value.isGameOver) {
        const hasGameOver = events.some(e => String(e.type).toUpperCase() === 'GAME_OVER')
        if (!hasGameOver) return
    }
    
    console.log('Events received:', JSON.stringify(events, null, 2))
    
    // Process events to find actions
    for (const event of events) {
        const eventType = String(event.type).toUpperCase() // Normalize to uppercase string
        
        // Handle face card turn retention tracking
        if (eventType === 'TURN_KEPT' && event.actor === 'PLAYER') {
            faceCardEffectActive.value = true
        } else if (eventType === 'TURN_ENDED' || (eventType === 'TURN_STARTED' && event.actor !== 'PLAYER')) {
            faceCardEffectActive.value = false
        }

        
        // Handle NUMSEVEN_SHAPE_SELECTION_REQUIRED event
        if (eventType === 'NUMSEVEN_SHAPE_SELECTION_REQUIRED' && event.actor === 'PLAYER') {
            console.log('🎴 NumSevenCard shape selection required')
            // Extract card info from event
            if (event.cardPlayed) {
                const cardInfo: CardInfo = {
                    id: (event.cardPlayed as any).id || Date.now(),
                    index: (event.cardPlayed as any).index || 0,
                    rank: String((event.cardPlayed as any).rank || (event.cardPlayed as any).getRank?.() || '7'),
                    shape: String((event.cardPlayed as any).shape || (event.cardPlayed as any).getShape?.() || '')
                }
                shapeSelectionCard.value = cardInfo
                // Find the card index in player's hand
                if (gameState.value?.playerHand) {
                    const cardIndex = gameState.value.playerHand.findIndex(c => 
                        c.rank === cardInfo.rank && c.shape === cardInfo.shape
                    )
                    if (cardIndex !== -1) {
                        pendingNumSevenCardIndex.value = cardIndex
                    }
                }
                showShapeSelection.value = true
            }
            return
        }
        
        // Handle TURN_KEPT event (face card was played, turn continues)
        if (eventType === 'TURN_KEPT' && event.actor === 'PLAYER') {
            console.log('🎯 Turn kept - showing transition animation')
            // Show transition animation indicating turn continues
            transitionPlayerName.value = 'PLAYER'
            showTurnTransition.value = true
            // Transition will auto-hide after animation completes
            return
        }
        
        // Handle GAME_OVER event
        if (eventType === 'GAME_OVER') {
            console.log('🏁 Game Over! Winner:', event.actor)
            // Wait a moment for any last animations to finish
            setTimeout(() => {
                gameOverWinner.value = event.actor || 'UNKNOWN'
            }, 1000)
            return
        }

        // Only process AI events when it's player's turn (AI just finished)
        if (!gameState.value.isPlayerTurn && event.actor === 'AI') {
            console.log('AI event detected:', eventType, event)
            
            // AI played a card (check multiple possible event types)
            if ((eventType.includes('CARD_PLAYED') || eventType.includes('ATTACK') || eventType.includes('DEFENSE')) && event.cardPlayed) {
                console.log('Animating AI card play from event:', event.cardPlayed)
                // Handle both CardInfo and raw Card object formats
                let cardInfo: CardInfo
                if (typeof event.cardPlayed === 'object') {
                    cardInfo = {
                        id: (event.cardPlayed as any).id || Date.now(),
                        index: (event.cardPlayed as any).index || 0,
                        rank: String((event.cardPlayed as any).rank || (event.cardPlayed as any).getRank?.() || ''),
                        shape: String((event.cardPlayed as any).shape || (event.cardPlayed as any).getShape?.() || '')
                    }
                } else {
                    // Fallback: use lastUsedCard from gameState
                    if (gameState.value?.lastUsedCard) {
                        cardInfo = gameState.value.lastUsedCard
                    } else {
                        console.warn('Could not extract card info from event')
                        continue
                    }
                }
                // Wait for transition overlay to fade out before animating
                setTimeout(() => {
                    animateCardPlay(cardInfo)
                }, 2500)
            }
            // AI drew card(s)
            else if (eventType.includes('CARD_DRAWN') || eventType.includes('DREW')) {
                const cardsDrawn = event.cardsDrawn || 1
                console.log('Animating AI card draw from event:', cardsDrawn)
                // Wait for transition overlay to fade out before animating
                setTimeout(() => {
                    animateCardDraw(cardsDrawn)
                }, 2500)
            }
        }
    }
}, { deep: true })

// Track when AI turn starts to capture initial state
watch(() => gameState.value?.isPlayerTurn, async (isPlayerTurn, oldValue) => {
    if (!gameState.value || gameState.value.isGameOver) return
    
    // When AI turn starts, capture the initial state
    if (oldValue === true && isPlayerTurn === false) {
        console.log('🎮 AI turn started - capturing initial state')
        previousAiHandSize.value = gameState.value.aiHandSize || 0
        previousLastUsedCardId.value = gameState.value.lastUsedCard?.id || null
        console.log('Initial state:', {
            aiHandSize: previousAiHandSize.value,
            lastCardId: previousLastUsedCardId.value
        })
    }
    
    // When turn switches from AI to Player, AI just finished - trigger animations AFTER overlay
    if (oldValue === false && isPlayerTurn === true) {
        console.log('✅ AI turn completed! Waiting for overlay to disappear...')
        
        // Start animation right after overlay starts fading (1.5s into the 2s animation)
        // This makes the card visible as the overlay fades out
        setTimeout(() => {
            console.log('🎬 Starting animations (overlay fading out)...')
            const aiHandSize = gameState.value?.aiHandSize || 0
            const lastCardId = gameState.value?.lastUsedCard?.id || null
            
            console.log('📊 State comparison:', {
                currentAiHandSize: aiHandSize,
                previousAiHandSize: previousAiHandSize.value,
                currentLastCardId: lastCardId,
                previousLastCardId: previousLastUsedCardId.value,
                lastUsedCard: gameState.value?.lastUsedCard
            })
            
            // Detect AI card play (lastUsedCard changed)
            if (lastCardId !== null && lastCardId !== previousLastUsedCardId.value && previousLastUsedCardId.value !== null) {
                // AI played a card - animate it
                if (gameState.value?.lastUsedCard) {
                    console.log('🎴 Animating AI card play:', gameState.value.lastUsedCard)
                    animateCardPlay(gameState.value.lastUsedCard)
                } else {
                    console.warn('⚠️ lastUsedCard is null but ID changed')
                }
            } else {
                console.log('ℹ️ No card play detected (lastCardId unchanged or null)')
            }
            
            // Detect AI card draw (AI hand size increased)
            if (aiHandSize > previousAiHandSize.value && previousAiHandSize.value >= 0) {
                const cardsDrawn = aiHandSize - previousAiHandSize.value
                console.log('🃏 Animating AI card draw:', cardsDrawn, 'cards')
                animateCardDraw(cardsDrawn)
            } else {
                console.log('ℹ️ No card draw detected (hand size:', aiHandSize, 'vs previous:', previousAiHandSize.value, ')')
            }
            
            // Update previous values for next turn
            previousAiHandSize.value = aiHandSize
            previousLastUsedCardId.value = lastCardId
        }, 1500) // Start animation 1.5s into overlay (while it's fading out)
    }
}, { immediate: false })

// Watch for turn changes - this is the main mechanism for auto-advancing turns
watch(() => gameState.value?.isPlayerTurn, async (isPlayerTurn) => {
    // Skip if game is not initialized or is over
    if (gameState.value === null || gameState.value.isGameOver || isPlayerTurn === undefined) {
        return
    }
    
    // Initialize previousPlayerTurn on first run
    if (previousPlayerTurn.value === null) {
        previousPlayerTurn.value = isPlayerTurn
        // Initialize AI hand size tracking
        if (gameState.value.aiHandSize !== undefined) {
            previousAiHandSize.value = gameState.value.aiHandSize
        }
        if (gameState.value.lastUsedCard?.id !== undefined) {
            previousLastUsedCardId.value = gameState.value.lastUsedCard.id
        }
        return
    }
    
    // Only trigger if turn actually changed
    if (previousPlayerTurn.value === isPlayerTurn) {
        return
    }
    
    // Update previous value
    previousPlayerTurn.value = isPlayerTurn
    
    // Check if AI is under attack (battle situation)
    if (!isPlayerTurn && gameState.value) {
        // AI's turn - check if they're under attack
        isBattleSituation.value = (gameState.value.accumulatedDraws ?? 0) > 0
        battleCardsToDraw.value = gameState.value.accumulatedDraws ?? 0
    } else {
        // Player's turn - no battle situation
        isBattleSituation.value = false
        battleCardsToDraw.value = 0
    }
    
    // Show turn transition
    transitionPlayerName.value = isPlayerTurn ? 'PLAYER' : 'AI'
    showTurnTransition.value = true
    
    // If it's AI's turn and we're not already processing, execute AI turn sequence
    if (!isPlayerTurn && !isProcessingAITurn.value) {
        processAiTurnSequence()
    }
}, { immediate: true })

// Process AI turn sequence (handles multiple steps if AI keeps turn e.g. Face Cards)
async function processAiTurnSequence() {
    if (isProcessingAITurn.value) return
    isProcessingAITurn.value = true
    
    // Initial delay for turn transition (2 seconds)
    await new Promise(resolve => setTimeout(resolve, 2000))

    try {
        // Loop while it's valid to keep playing (AI turn, not game over)
        // We check condition at start of loop
        while (gameState.value && !gameState.value.isPlayerTurn && !gameState.value.isGameOver) {
            console.log('🤖 Executing AI turn step...')
            await executeAITurn()
            
            // If game is over after move, break immediately
            if (!gameState.value || gameState.value.isGameOver) {
                break
            }

            // If it's STILL AI turn (e.g. Face Card played), wait for animation before next move
            if (!gameState.value.isPlayerTurn) {
                console.log('🔄 AI kept turn (Face Card or combo), waiting for animation...')
                // Wait for animation to finish (approx 2.5s to match event watcher delays)
                // This creates the "step-by-step" visualization effect
                await new Promise(resolve => setTimeout(resolve, 3000))
            }
        }
    } catch (e) {
        console.error('Error in AI turn sequence:', e)
    } finally {
        isProcessingAITurn.value = false
    }
}

// Animate AI playing a card
function animateCardPlay(card: CardInfo) {
    console.log('🎴 animateCardPlay called with:', card)
    
    // Small delay to ensure DOM is ready
    setTimeout(() => {
        // Get AI panel position (source)
        const aiPanel = document.querySelector('.player-panel.other-player')
        const discardPile = document.querySelector('.used-card-pile')
        
        console.log('DOM elements found:', { aiPanel: !!aiPanel, discardPile: !!discardPile })
        
        if (!aiPanel || !discardPile) {
            console.warn('⚠️ Could not find DOM elements for animation')
            // Retry once
            setTimeout(() => {
                const retryAiPanel = document.querySelector('.player-panel.other-player')
                const retryDiscardPile = document.querySelector('.used-card-pile')
                if (retryAiPanel && retryDiscardPile) {
                    animateCardPlay(card)
                }
            }, 200)
            return
        }
        
        const aiRect = aiPanel.getBoundingClientRect()
        const discardRect = discardPile.getBoundingClientRect()
        
        console.log('📍 Animation positions:', {
            from: { x: aiRect.left + aiRect.width / 2, y: aiRect.top + aiRect.height / 2 },
            to: { x: discardRect.left + discardRect.width / 2, y: discardRect.top + discardRect.height / 2 }
        })
        
        // Set initial state
        animatingCard.value = {
            card,
            type: 'play',
            from: {
                x: aiRect.left + aiRect.width / 2,
                y: aiRect.top + aiRect.height / 2
            },
            to: {
                x: discardRect.left + discardRect.width / 2,
                y: discardRect.top + discardRect.height / 2
            },
            isAnimating: false
        }
        
        // Start animation immediately
        setTimeout(() => {
            animatingCard.value.isAnimating = true
            console.log('✅ Animation started!')
        }, 50)
        
        // Clear animation after it completes
        setTimeout(() => {
            animatingCard.value = { card: null, type: null, from: null, to: null, isAnimating: false }
            console.log('🧹 Animation cleared')
        }, 1200)
    }, 100)
}

// Animate AI drawing a card
function animateCardDraw(cardsDrawn: number) {
    console.log('🃏 animateCardDraw called with:', cardsDrawn)
    
    setTimeout(() => {
        const deck = document.querySelector('.deck')
        const aiPanel = document.querySelector('.player-panel.other-player')
        
        console.log('DOM elements found:', { deck: !!deck, aiPanel: !!aiPanel })
        
        if (!deck || !aiPanel) {
            console.warn('⚠️ Could not find DOM elements for draw animation')
            // Retry once
            setTimeout(() => {
                const retryDeck = document.querySelector('.deck')
                const retryAiPanel = document.querySelector('.player-panel.other-player')
                if (retryDeck && retryAiPanel) {
                    animateCardDraw(cardsDrawn)
                }
            }, 200)
            return
        }
        
        const deckRect = deck.getBoundingClientRect()
        const aiRect = aiPanel.getBoundingClientRect()
        
        // Animate each card being drawn
        for (let i = 0; i < cardsDrawn; i++) {
            setTimeout(() => {
                animatingCard.value = {
                    card: {
                        id: -9999 - i,
                        index: -1,
                        rank: '',
                        shape: ''
                    },
                    type: 'draw',
                    from: {
                        x: deckRect.left + deckRect.width / 2,
                        y: deckRect.top + deckRect.height / 2
                    },
                    to: {
                        x: aiRect.left + aiRect.width / 2,
                        y: aiRect.top + aiRect.height / 2
                    },
                    isAnimating: false
                }
                
                // Start animation immediately
                setTimeout(() => {
                    animatingCard.value.isAnimating = true
                    console.log('✅ Draw animation started for card', i + 1)
                }, 50)
                
                // Clear animation after it completes
                setTimeout(() => {
                    if (animatingCard.value.type === 'draw') {
                        animatingCard.value = { card: null, type: null, from: null, to: null, isAnimating: false }
                    }
                }, 800)
            }, i * 200) // Stagger multiple card draws
        }
    }, 100)
}

function onTransitionComplete() {
    showTurnTransition.value = false
    
    // After transition completes, check if we need to animate AI actions
    // This happens when AI turn just finished
    if (gameState.value?.isPlayerTurn) {
        setTimeout(() => {
            console.log('🎬 Transition complete, checking for AI animations...')
            const aiHandSize = gameState.value?.aiHandSize || 0
            const lastCardId = gameState.value?.lastUsedCard?.id || null
            
            // Detect AI card play (lastUsedCard changed)
            if (lastCardId !== null && lastCardId !== previousLastUsedCardId.value && previousLastUsedCardId.value !== null) {
                if (gameState.value?.lastUsedCard) {
                    console.log('🎴 Animating AI card play:', gameState.value.lastUsedCard)
                    animateCardPlay(gameState.value.lastUsedCard)
                }
            }
            
            // Detect AI card draw (AI hand size increased)
            if (aiHandSize > previousAiHandSize.value && previousAiHandSize.value >= 0) {
                const cardsDrawn = aiHandSize - previousAiHandSize.value
                console.log('🃏 Animating AI card draw:', cardsDrawn)
                animateCardDraw(cardsDrawn)
            }
            
            // Update tracking values
            previousAiHandSize.value = aiHandSize
            previousLastUsedCardId.value = lastCardId
        }, 200) // Small delay to ensure overlay is fully gone and visible
    }
}

async function handleDraw() {
    // When player draws ("0"), backend automatically:
    // 1. Draws the card
    // 2. Ends player's turn (advances to AI)
    // 3. Returns updated game state with isPlayerTurn = false
    // The watcher will detect the turn change and automatically execute AI turn
    await playAction('0')
}

async function handleOneCard() {
    // One card button clicked - could emit a message or handle differently
    console.log('One Card! button clicked')
}

async function handlePlayCards(indices: number[]) {
    // Validate indices are valid
    if (!indices || indices.length === 0) {
        console.error('No indices provided to handlePlayCards')
        return
    }
    
    // Validate game state exists
    if (!gameState.value || !gameState.value.playerHand) {
        console.error('Game state or player hand not available')
        return
    }
    
    // Filter out any invalid indices and validate they're within bounds
    const handSize = gameState.value.playerHand.length
    const validIndices = indices.filter(idx => {
        if (typeof idx !== 'number' || idx < 0 || Number.isNaN(idx)) {
            return false
        }
        // Validate index is within hand bounds
        if (idx >= handSize) {
            console.warn(`Index ${idx} is out of bounds (hand size: ${handSize})`)
            return false
        }
        return true
    })
    
    if (validIndices.length === 0) {
        console.error('No valid indices after filtering:', indices, 'hand size:', handSize)
        return
    }
    
    // If some indices were filtered out, warn the user
    if (validIndices.length !== indices.length) {
        console.warn(`Some indices were invalid. Using valid indices: ${validIndices} from original: ${indices}`)
    }
    
    // Check if NumSevenCard is being played (single card, rank 7)
    if (validIndices.length === 1) {
        const cardIndex = validIndices[0]
        const card = gameState.value.playerHand[cardIndex]
        if (card && card.rank === '7') {
            // NumSevenCard selected - trigger shape selection
            // Send the card index to backend, which will return shape selection event
            const action = String(cardIndex + 1) // Backend expects 1-indexed
            console.log('NumSevenCard selected, requesting shape selection:', action, 'card:', card)
            await playAction(action)
            return
        }
    }
    
    // Convert indices to action string (backend expects 1-indexed)
    const action = validIndices.map(idx => idx + 1).join(',')
    console.log('Playing cards with action:', action, 'from indices:', validIndices, 'hand size:', handSize)
    
    // When player plays cards, backend automatically ends turn and advances to AI
    // The watcher will detect the turn change and automatically execute AI turn
    await playAction(action)
}

// Handle shape selection for NumSevenCard
async function handleShapeSelection(shapeCode: number) {
    if (pendingNumSevenCardIndex.value === null) {
        console.error('No pending NumSevenCard for shape selection')
        return
    }
    
    // Send shape selection action (format: "SHAPE:1" where 1-4 are shape codes)
    const action = `SHAPE:${shapeCode}`
    console.log('Selecting shape:', action, 'for card at index:', pendingNumSevenCardIndex.value)
    
    showShapeSelection.value = false
    pendingNumSevenCardIndex.value = null
    shapeSelectionCard.value = null
    
    await playAction(action)
}

// Refresh game state periodically or on demand
async function refreshState() {
    await fetchGameState()
}

// Create placeholder cards for AI hand (face down)
const aiHandCards = computed<CardInfo[]>(() => {
    if (!gameState.value) return []
    const cards: CardInfo[] = []
    for (let i = 0; i < gameState.value.aiHandSize; i++) {
        cards.push({
            id: -1000 - i, // Negative IDs to avoid conflicts
            index: i,
            rank: '',
            shape: ''
        })
    }
    return cards
})
</script>

<template>
    <div class="game-board">
        <!-- Turn Transition Overlay -->
        <TurnTransition 
            v-if="showTurnTransition"
            :playerName="transitionPlayerName"
            :isBattle="isBattleSituation"
            :cardsToDraw="battleCardsToDraw"
        />

        <GameOverOverlay
            v-if="gameOverWinner"
            :winner="gameOverWinner"
        />
        
        <!-- NumSevenCard Shape Selection Modal -->
        <Transition name="shape-modal">
            <div v-if="showShapeSelection" class="shape-selection-overlay" @click.self="showShapeSelection = false">
                <div class="shape-selection-modal">
                    <h3 class="shape-selection-title">Select a Shape for the 7 Card</h3>
                    <div class="shape-selection-grid">
                        <button 
                            class="shape-btn hearts" 
                            @click="handleShapeSelection(1)"
                        >
                            <span class="shape-icon">♥</span>
                            <span class="shape-name">Hearts</span>
                        </button>
                        <button 
                            class="shape-btn diamonds" 
                            @click="handleShapeSelection(2)"
                        >
                            <span class="shape-icon">♦</span>
                            <span class="shape-name">Diamonds</span>
                        </button>
                        <button 
                            class="shape-btn clubs" 
                            @click="handleShapeSelection(3)"
                        >
                            <span class="shape-icon">♣</span>
                            <span class="shape-name">Clubs</span>
                        </button>
                        <button 
                            class="shape-btn spades" 
                            @click="handleShapeSelection(4)"
                        >
                            <span class="shape-icon">♠</span>
                            <span class="shape-name">Spades</span>
                        </button>
                    </div>
                </div>
            </div>
        </Transition>
        
        <div v-if="gameState">
            <!-- Turn Indicator Banner -->
            <div v-if="gameState.isPlayerTurn" class="turn-banner player-turn">
                <span class="turn-icon">🎯</span>
                <span class="turn-text">YOUR TURN</span>
            </div>
            <div v-else-if="!gameState.isGameOver" class="turn-banner ai-turn">
                <span class="turn-icon">🤖</span>
                <span class="turn-text">AI'S TURN</span>
            </div>
            
            <div class="game-info">
                <p>Deck Size: {{ gameState.deckSize }}</p>
                <p>AI Hand Size: {{ gameState.aiHandSize }}</p>
                <p>Accumulated Draws: {{ gameState.accumulatedDraws }}</p>
                <p v-if="gameState.isGameOver">Game Over! Winner: {{ gameState.winner }}</p>
            </div>
            
            <div class="game-area">
                <Deck :deckSize="gameState.deckSize" />
                <UsedCardPile 
                    :lastUsedCard="gameState.lastUsedCard" 
                    :usedCardPile="gameState.usedCardPile"
                    :accumulatedDraws="gameState.accumulatedDraws"
                    :isInitialTurn="gameState.isInitialTurn"
                />
            </div>
            
            <!-- AI Panel (shows card backs) -->
            <PlayerPanel
                :cards="aiHandCards"
                :isPlayerTurn="!gameState.isPlayerTurn"
                :isOwnPanel="false"
                :accumulatedDraws="gameState.accumulatedDraws"
                :lastUsedCard="gameState.lastUsedCard"
                :isInitialTurn="gameState.isInitialTurn"
                :isGameOver="gameState?.isGameOver"
                playerName="AI"
            />
            
            <!-- Player Panel (shows own cards) -->
            <PlayerPanel
                :cards="gameState.playerHand"
                :isPlayerTurn="gameState.isPlayerTurn"
                :playableCardIndices="gameState.playableCardIndices"
                :accumulatedDraws="gameState.accumulatedDraws"
                :lastUsedCard="gameState.lastUsedCard"
                :isInitialTurn="gameState?.isInitialTurn"
                :isOwnPanel="true"
                :faceCardEffectActive="faceCardEffectActive"
                :isGameOver="gameState?.isGameOver"
                playerName="You"
                @draw="handleDraw"
                @oneCard="handleOneCard"
                @playCards="handlePlayCards"
            />
        </div>
        
        <div v-else>
            <p>Loading game...</p>
        </div>
        
        <!-- Animated card for AI actions -->
        <Transition name="card-fade">
            <div
                v-if="animatingCard.card && animatingCard.from && animatingCard.to"
                ref="animatedCardEl"
                class="animated-card"
                :class="{ 'animating': animatingCard.isAnimating }"
                :style="{
                    position: 'fixed',
                    left: animatingCard.from.x + 'px',
                    top: animatingCard.from.y + 'px',
                    '--to-x': (animatingCard.to.x - animatingCard.from.x) + 'px',
                    '--to-y': (animatingCard.to.y - animatingCard.from.y) + 'px',
                    zIndex: 99999,
                    pointerEvents: 'none',
                    willChange: 'transform',
                    opacity: '1',
                    visibility: 'visible'
                }"
            >
                <Card 
                    :card="animatingCard.card" 
                    :showBack="animatingCard.type === 'draw'"
                />
            </div>
        </Transition>
    </div>
</template>

<style lang='css' scoped>
.game-board {
    padding: 20px;
}

.refresh-btn {
    padding: 8px 16px;
    margin-bottom: 20px;
    background: #6366f1;
    color: white;
    border: none;
    border-radius: 6px;
    cursor: pointer;
}

.refresh-btn:hover {
    background: #4f46e5;
}

.turn-banner {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 16px 24px;
    border-radius: 12px;
    font-weight: bold;
    font-size: 20px;
    margin-bottom: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    animation: turn-banner-pulse 2s infinite;
}

.turn-banner.player-turn {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.5);
}

.turn-banner.ai-turn {
    background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
    color: white;
    box-shadow: 0 4px 16px rgba(107, 114, 128, 0.5);
}

.turn-icon {
    font-size: 24px;
    animation: turn-icon-bounce 1.5s ease-in-out infinite;
}

.turn-text {
    letter-spacing: 2px;
    text-transform: uppercase;
}

@keyframes turn-banner-pulse {
    0%, 100% {
        transform: scale(1);
        box-shadow: 0 4px 16px rgba(59, 130, 246, 0.5);
    }
    50% {
        transform: scale(1.02);
        box-shadow: 0 6px 20px rgba(59, 130, 246, 0.7);
    }
}

@keyframes turn-icon-bounce {
    0%, 100% {
        transform: translateY(0) rotate(0deg);
    }
    25% {
        transform: translateY(-5px) rotate(-5deg);
    }
    75% {
        transform: translateY(-5px) rotate(5deg);
    }
}

.game-info {
    background: #f3f4f6;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
}

.game-info p {
    margin: 5px 0;
}

.game-area {
    display: flex;
    justify-content: space-around;
    align-items: center;
    margin: 20px 0;
    gap: 40px;
    flex-wrap: wrap;
}

/* Animated card styles */
.animated-card {
    transform: translate(-50%, -50%);
    transition: none;
    opacity: 1 !important;
    visibility: visible !important;
}

.animated-card.animating {
    animation: cardMove 1s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}

.card-fade-enter-active {
    transition: opacity 0.1s ease;
}

.card-fade-leave-active {
    transition: opacity 0.3s ease;
}

.card-fade-enter-from {
    opacity: 0;
}

.card-fade-enter-to {
    opacity: 1;
}

.card-fade-leave-to {
    opacity: 0;
}

@keyframes cardMove {
    0% {
        transform: translate(-50%, -50%) scale(1) rotate(0deg);
        opacity: 1;
    }
    50% {
        transform: translate(
            calc(var(--to-x, 0px) - 50%), 
            calc(var(--to-y, 0px) - 50%)
        ) scale(1.15) rotate(8deg);
        opacity: 1;
    }
    100% {
        transform: translate(
            calc(var(--to-x, 0px) - 50%), 
            calc(var(--to-y, 0px) - 50%)
        ) scale(1) rotate(0deg);
        opacity: 0.9;
    }
}

/* Shape Selection Modal Styles */
.shape-selection-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    backdrop-filter: blur(4px);
}

.shape-selection-modal {
    background: white;
    border-radius: 20px;
    padding: 40px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    max-width: 500px;
    width: 90%;
    animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.shape-selection-title {
    text-align: center;
    margin-bottom: 30px;
    font-size: 24px;
    font-weight: bold;
    color: #1f2937;
}

.shape-selection-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
}

.shape-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 30px 20px;
    border: 3px solid transparent;
    border-radius: 15px;
    background: #f9fafb;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
}

.shape-btn:hover {
    transform: scale(1.1) translateY(-5px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.shape-btn.hearts {
    border-color: #ef4444;
}

.shape-btn.hearts:hover {
    background: #fee2e2;
    border-color: #dc2626;
}

.shape-btn.diamonds {
    border-color: #ef4444;
}

.shape-btn.diamonds:hover {
    background: #fee2e2;
    border-color: #dc2626;
}

.shape-btn.clubs {
    border-color: #1f2937;
}

.shape-btn.clubs:hover {
    background: #f3f4f6;
    border-color: #111827;
}

.shape-btn.spades {
    border-color: #1f2937;
}

.shape-btn.spades:hover {
    background: #f3f4f6;
    border-color: #111827;
}

.shape-icon {
    font-size: 48px;
    margin-bottom: 10px;
    line-height: 1;
}

.shape-name {
    font-size: 16px;
    text-transform: uppercase;
    letter-spacing: 1px;
}

/* Modal transition animations */
.shape-modal-enter-active {
    transition: opacity 0.3s ease;
}

.shape-modal-leave-active {
    transition: opacity 0.2s ease;
}

.shape-modal-enter-from,
.shape-modal-leave-to {
    opacity: 0;
}

.shape-modal-enter-active .shape-selection-modal {
    animation: modalSlideIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.shape-modal-leave-active .shape-selection-modal {
    animation: modalSlideOut 0.2s ease;
}

@keyframes modalSlideIn {
    0% {
        transform: scale(0.8) translateY(-20px);
        opacity: 0;
    }
    100% {
        transform: scale(1) translateY(0);
        opacity: 1;
    }
}

@keyframes modalSlideOut {
    0% {
        transform: scale(1) translateY(0);
        opacity: 1;
    }
    100% {
        transform: scale(0.8) translateY(-20px);
        opacity: 0;
    }
}
</style>