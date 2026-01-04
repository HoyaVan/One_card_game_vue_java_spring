<script setup lang="ts">
import { onMounted, watch, computed, ref } from 'vue'
import Deck from './Deck.vue'
import UsedCardPile from './UsedCardPile.vue'
import PlayerPanel from './PlayerPanel.vue'
import Hands from './hands.vue'
import TurnTransition from './TurnTransition.vue'
import { useOneCardGame } from '../api/useOneCardGame'
import type { CardInfo } from '../types/card'

const { gameState, setupGame, playAction, executeAITurn, fetchGameState, lastEvents } = useOneCardGame()

// Turn transition state
const showTurnTransition = ref(false)
const transitionPlayerName = ref('PLAYER')
const lastProcessedEventId = ref<string | null>(null)

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
    if (!events || !gameState.value || gameState.value.isGameOver) return
    
    // Only process events when it's player's turn (AI just finished)
    if (!gameState.value.isPlayerTurn) return
    
    console.log('Events received after AI turn:', JSON.stringify(events, null, 2))
    
    // Wait for transition overlay to fade out before animating
    setTimeout(() => {
        // Process events to find AI actions
        for (const event of events) {
            if (event.actor === 'AI') {
                const eventType = String(event.type).toUpperCase() // Normalize to uppercase string
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
                    animateCardPlay(cardInfo)
                }
                // AI drew card(s)
                else if (eventType.includes('CARD_DRAWN') || eventType.includes('DREW')) {
                    const cardsDrawn = event.cardsDrawn || 1
                    console.log('Animating AI card draw from event:', cardsDrawn)
                    animateCardDraw(cardsDrawn)
                }
            }
        }
    }, 2500) // Wait for transition overlay to disappear
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
    
    // Show turn transition
    transitionPlayerName.value = isPlayerTurn ? 'PLAYER' : 'AI'
    showTurnTransition.value = true
    
    // If it's AI's turn and we're not already processing, execute AI turn
    if (!isPlayerTurn && !isProcessingAITurn.value) {
        isProcessingAITurn.value = true
        // Wait for transition animation (2 seconds) then execute AI turn
        setTimeout(async () => {
            if (gameState.value && !gameState.value.isPlayerTurn && !gameState.value.isGameOver) {
                // Execute AI turn - this will update gameState and trigger animations
                await executeAITurn()
                // After AI turn completes, animations will trigger via state watchers
            }
            isProcessingAITurn.value = false
        }, 2000)
    }
}, { immediate: true })

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
    // Convert indices to action string (backend expects 1-indexed)
    const action = indices.map(idx => idx + 1).join(',')
    // When player plays cards, backend automatically ends turn and advances to AI
    // The watcher will detect the turn change and automatically execute AI turn
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
            :show="showTurnTransition" 
            :playerName="transitionPlayerName"
            @transition-complete="onTransitionComplete"
        />
        
        
        <div v-if="gameState">
            <div class="game-info">
                <p>Deck Size: {{ gameState.deckSize }}</p>
                <p>AI Hand Size: {{ gameState.aiHandSize }}</p>
                <p>Accumulated Draws: {{ gameState.accumulatedDraws }}</p>
                <p>Player Turn: {{ gameState.isPlayerTurn ? 'Yes' : 'No' }}</p>
                <p v-if="gameState.isGameOver">Game Over! Winner: {{ gameState.winner }}</p>
            </div>
            
            <div class="game-area">
                <Deck :deckSize="gameState.deckSize" />
                <UsedCardPile 
                    :lastUsedCard="gameState.lastUsedCard" 
                    :usedCardPile="gameState.usedCardPile"
                />
            </div>
            
            <!-- AI Panel (shows card backs) -->
            <PlayerPanel
                :cards="aiHandCards"
                :isPlayerTurn="!gameState.isPlayerTurn"
                :lastUsedCard="gameState.lastUsedCard"
                :accumulatedDraws="gameState.accumulatedDraws"
                :isOwnPanel="false"
                playerName="AI"
            />
            
            <!-- Player Panel (shows own cards) -->
            <PlayerPanel
                :cards="gameState.playerHand"
                :isPlayerTurn="gameState.isPlayerTurn"
                :lastUsedCard="gameState.lastUsedCard"
                :accumulatedDraws="gameState.accumulatedDraws"
                :playableCardIndices="gameState.playableCardIndices"
                :isOwnPanel="true"
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
</style>