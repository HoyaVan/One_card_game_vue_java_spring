<script setup lang="ts">
    import { ref, computed, watch } from 'vue'
    import Card from './Card.vue'
    import type { CardInfo } from '../types/card'
    import { isFaceCard, isDefenseCard } from '../utils/cardHelpers'

    const props = withDefaults(defineProps<{
        cards?: CardInfo[]
        isPlayerTurn?: boolean
        playableCardIndices?: number[]  // Backend-provided playable card indices
        isOwnPanel?: boolean  // Whether this is the player's own panel (true) or another player's panel (false)
        playerName?: string  // Name of the player (e.g., "Player", "AI")
        accumulatedDraws?: number  // Number of cards that must be drawn (attack state)
        lastUsedCard?: CardInfo | null  // Last card played (to check if it's an attack card)
        isInitialTurn?: boolean  // Whether this is the initial turn (attack rules don't apply)
        faceCardEffectActive?: boolean // Whether the player kept the turn via face card effect
        isGameOver?: boolean // Whether the game is over
    }>(), {
        cards: () => []
    })

    const emit = defineEmits<{
        draw: []
        oneCard: []
        playCards: [indices: number[]]
    }>()

    // Track selected card IDs in order of selection (preserves selection order)
    const selectedIds = ref<number[]>([])

    // Clear selection when turn ends (isPlayerTurn becomes false)
    watch(() => props.isPlayerTurn, (isPlayerTurn) => {
        if (!isPlayerTurn) {
            selectedIds.value = []
        }
    })

    // Clear selection when playableCardIndices change (e.g., after drawing a card or playing cards)
    // This ensures selection is cleared when game state changes
    watch(() => props.playableCardIndices, () => {
        if (selectedIds.value.length > 0) {
            // Check if any selected cards are still playable
            const selectedCards = selectedIds.value
                .map(id => props.cards?.find(c => c.id === id))
                .filter((c): c is CardInfo => c !== undefined)
            
            const stillPlayable = selectedCards.some(card => 
                props.playableCardIndices?.includes(card.index)
            )
            
            // If no selected cards are playable anymore, clear selection
            if (!stillPlayable) {
                selectedIds.value = []
            }
        }
    })

    // Clear selection when cards change (e.g., cards removed from hand)
    watch(() => props.cards, () => {
        if (selectedIds.value.length > 0) {
            // Check if any selected card IDs still exist in the new cards array
            const validSelectedIds = selectedIds.value.filter(id => 
                props.cards?.some(card => card.id === id)
            )
            // If some selected cards are no longer in the hand, clear selection
            if (validSelectedIds.length !== selectedIds.value.length) {
                selectedIds.value = []
            }
        }
    }, { deep: true })

    function toggleSelect(id: number) {
        // Only allow selecting playable cards
        if (!props.isPlayerTurn) return
        
        const card = props.cards?.find(c => c.id === id)
        if (!card) return
        
        // Check if card is playable
        if (!isCardPlayable(card)) return
        
        const index = selectedIds.value.indexOf(id)
        if (index !== -1) {
            // Deselect: remove from array
            selectedIds.value.splice(index, 1)
        } else {
            // Select: add to end of array (preserves order)
            selectedIds.value.push(id)
        }
    }

    // Convert selected IDs back to indices in selection order for backend API call
    // IMPORTANT: Use current card positions in the hand, not stored indices
    // This ensures indices are correct even if the hand has changed
    function getSelectedIndices(): number[] {
        if (!props.cards || selectedIds.value.length === 0) return []
        
        // Find cards by ID and get their CURRENT indices in the hand
        // This handles cases where the hand has changed (cards played, drawn, etc.)
        const indices: number[] = []
        for (const id of selectedIds.value) {
            // Find the card with this ID in the current hand
            const cardIndex = props.cards.findIndex(card => card.id === id)
            if (cardIndex >= 0) {
                indices.push(cardIndex) // Use 0-based index internally, GameBoard will handle backend offset
            } else {
                console.warn('Card ID not found in current hand:', id, 'Available cards:', props.cards?.map(c => ({ id: c.id, index: c.index })))
            }
        }
        
        // Validate indices are within bounds
        const handSize = props.cards.length
        const validIndices = indices.filter(idx => idx >= 0 && idx < handSize)
        
        if (validIndices.length !== indices.length) {
            console.warn('Some indices are out of bounds:', {
                selectedIds: selectedIds.value,
                indices,
                handSize,
                validIndices,
                cards: props.cards.map(c => ({ id: c.id, index: c.index }))
            })
        }
        
        return validIndices
    }

    // Check if THIS player is under attack (must defend)
    // Attack rules don't apply on initial turn, even if the initial card is an attack card
    // A player is under attack if:
    // 1. accumulatedDraws > 0 (someone is attacking)
    // 2. It's THIS player's turn (the attack targets the player whose turn it is)
    // 3. NOT the initial turn
    const isUnderAttack = computed(() => {
        if (props.isInitialTurn || props.isGameOver) return false  // No attack warnings if game is over
        // Only show attack warning if it's THIS player's turn AND accumulatedDraws > 0
        return props.isPlayerTurn && 
               props.accumulatedDraws !== undefined && 
               props.accumulatedDraws > 0
    })

    // Check if face card effect is active for the player
    // Face card effect ONLY applies if:
    // 1. It's the player's turn (isPlayerTurn === true)
    // 2. A face card is on the table
    // 3. It's NOT the initial card
    // This ensures the effect only applies when the PLAYER played the face card,
    // not when AI played it (because when AI plays it, isPlayerTurn would be false)
    const isFaceCardOnTable = computed(() => {
        // Use authoritative prop from backend events (via GameBoard) if available
        if (props.faceCardEffectActive !== undefined) {
             return props.faceCardEffectActive && props.isPlayerTurn
        }

        // Fallback logic (legacy support or if event missing)
        // Only show face card effect during player's turn
        if (!props.isPlayerTurn) {
            return false // Not player's turn, so no face card effect
        }
        if (!props.lastUsedCard || !isFaceCard(props.lastUsedCard)) {
            return false
        }
        // Check if it's the initial card - if so, face card effect doesn't apply
        if (props.lastUsedCard.isInitialCard === true) {
            return false
        }
        // Face card is on table AND it's player's turn = player just played it (turn kept)
        return true
    })

    // Check if a card is a defense card that can be played
    // Must be: under attack, can defend, AND actually playable according to backend
    function isDefenseCardPlayable(card: CardInfo): boolean {
        if (!isUnderAttack.value || !props.lastUsedCard) return false
        
        // Card must be able to defend against the attack
        if (!isDefenseCard(card, props.lastUsedCard)) return false
        
        // Card must also be in the playable indices from backend
        // This ensures the card is actually playable (meets all game rules)
        if (props.playableCardIndices && props.playableCardIndices.length > 0) {
            return props.playableCardIndices.includes(card.index)
        }
        
        return false
    }

    // Check if player has any playable defense cards
    const hasPlayableDefenseCards = computed(() => {
        if (!isUnderAttack.value || !props.lastUsedCard || !props.cards) return false
        
        return props.cards.some(card => isDefenseCardPlayable(card))
    })

    // Check if draw button should show outline (when under attack but no defense cards available)
    const shouldHighlightDrawButton = computed(() => {
        return isUnderAttack.value && !hasPlayableDefenseCards.value && props.isPlayerTurn
    })

    // Check if a card is playable using backend data and dynamic selection rules
    function isCardPlayable(card: CardInfo): boolean {
        // Only show playable indicator during player's turn
        if (!props.isPlayerTurn) {
            return false
        }

        // BATTLE STAGE ENFORCEMENT: When under attack (accumulatedDraws > 0),
        // ONLY defense cards are playable. This rule is strictly enforced for both AI and user.
        if (isUnderAttack.value) {
            // In battle stage, only defense cards can be played
            // Must be a valid defense card AND in the backend's playable indices
            if (!isDefenseCardPlayable(card)) {
                return false
            }
            
            // If cards are already selected, ensure they're all defense cards
            if (selectedIds.value.length > 0) {
                const selectedCards = selectedIds.value
                    .map(id => props.cards?.find(c => c.id === id))
                    .filter((c): c is CardInfo => c !== undefined)
                
                // All selected cards must be defense cards
                const allSelectedAreDefense = selectedCards.every(selectedCard => 
                    isDefenseCardPlayable(selectedCard)
                )
                
                if (!allSelectedAreDefense) {
                    return false // Can't add non-defense cards when defense cards are selected
                }
                
                // If adding another card, it must also be a defense card
                // And must match the first selected card's rank (for multi-card plays)
                const firstSelected = selectedCards[0]
                if (firstSelected) {
                    // Allow same rank defense cards for multi-card plays
                    return isDefenseCardPlayable(card) && card.rank === firstSelected.rank
                }
            }
            
            // No cards selected yet - only allow defense cards
            return isDefenseCardPlayable(card)
        }

        // NORMAL STAGE: Use normal matching rules when not under attack
        // FACE CARD EFFECT: If last card is a face card, ANY card is playable (one more turn effect)
        
        // If no cards selected, use backend-provided playable card indices
        // OR if face card is on table, all cards are playable
        if (selectedIds.value.length === 0) {
            // Normal rules: use backend-provided playable card indices
            if (props.playableCardIndices && props.playableCardIndices.length > 0) {
                return props.playableCardIndices.includes(card.index)
            }
            return false
        }

        // Get selected cards in order
        const selectedCards = selectedIds.value
            .map(id => props.cards?.find(c => c.id === id))
            .filter((c): c is CardInfo => c !== undefined)

        if (selectedCards.length === 0) return false

        const firstSelected = selectedCards[0]

        // Rule 1: If first selected card is a face card
        if (isFaceCard(firstSelected)) {
            const firstShape = firstSelected.shape.toLowerCase()
            const firstRank = firstSelected.rank.toLowerCase()
            
            // Check if all selected cards so far are face cards of the same rank
            const allFaceCardsSameRank = selectedCards.every(c => 
                isFaceCard(c) && c.rank.toLowerCase() === firstRank
            )
            
            // Check if all selected cards so far are face cards of the same shape
            const allFaceCardsSameShape = selectedCards.every(c => 
                isFaceCard(c) && c.shape.toLowerCase() === firstShape
            )

            // Check if a normal card (non-face card) has been selected
            const hasNormalCard = selectedCards.some(c => !isFaceCard(c))

            if (hasNormalCard) {
                // Rule 2: After face cards, if normal card selected, allow same rank normal cards
                const firstNormalCard = selectedCards.find(c => !isFaceCard(c))
                if (firstNormalCard) {
                    // Only allow normal cards with same rank
                    return !isFaceCard(card) && card.rank === firstNormalCard.rank
                }
            } else if (isFaceCard(card)) {
                // Adding another face card
                // Get the last selected card (most recently added)
                const lastSelected = selectedCards[selectedCards.length - 1]
                const lastShape = lastSelected.shape.toLowerCase()
                const lastRank = lastSelected.rank.toLowerCase()
                
                // Check if the new card matches the last card's rank (same rank, can be different shapes)
                if (card.rank.toLowerCase() === lastRank) {
                    // Same rank as last card - allow (e.g., Q Spades -> Q Hearts is OK)
                    return true
                }
                
                // Different rank - must match the last card's shape
                // This ensures Q (Spades) -> Q (Hearts) -> J (Hearts) is allowed
                // but Q (Spades) -> Q (Hearts) -> J (Spades) is NOT allowed
                return card.shape.toLowerCase() === lastShape
            } else {
                // Adding a normal card - must match the shape of the last selected card
                const lastSelected = selectedCards[selectedCards.length - 1]
                return card.shape.toLowerCase() === lastSelected.shape.toLowerCase()
            }
        } else {
            // Rule 3: If first selected card is a normal card, allow same rank cards
            // Same rank cards can always be dropped together (backend validates first card is playable)
            if (!isFaceCard(firstSelected)) {
                // Allow any card with the same rank as the first selected card
                // Backend will validate that the first card is playable when dropping
                return card.rank === firstSelected.rank
            }
        }

        // Fallback: use backend-provided playable card indices
        if (props.playableCardIndices && props.playableCardIndices.length > 0) {
            return props.playableCardIndices.includes(card.index)
        }

        return false
    }

    // Check if player has only one card left
    const hasOneCard = computed(() => {
        return props.cards?.length === 1
    })

    // Check if selected cards are valid for play
    // In battle stage, only defense cards are valid
    // After face card, any cards are valid
    const areSelectedCardsValid = computed(() => {
        if (selectedIds.value.length === 0) return false
        
        const selectedCards = selectedIds.value
            .map(id => props.cards?.find(c => c.id === id))
            .filter((c): c is CardInfo => c !== undefined)
        
        if (selectedCards.length === 0) return false
        
        // In battle stage, all selected cards must be defense cards
        if (isUnderAttack.value) {
            // All selected cards must be playable defense cards
            return selectedCards.every(card => isDefenseCardPlayable(card))
        }
        
        // In normal stage, check if cards can be dropped together
        // Rule: Same rank cards can be dropped together if the first card is playable
        const firstCard = selectedCards[0]
        if (firstCard) {
            // Check if first card is playable
            const firstCardIsPlayable = props.playableCardIndices && 
                props.playableCardIndices.includes(firstCard.index)
            
            if (firstCardIsPlayable) {
                // If first card is playable, allow dropping if all cards have same rank
                // This works for both face cards and normal cards
                // Backend validates this - frontend just needs to allow same-rank selection
                const allSameRank = selectedCards.every(card => 
                    card.rank.toLowerCase() === firstCard.rank.toLowerCase()
                )
                if (allSameRank) {
                    return true // Same rank cards can be dropped together (including face cards)
                }
                
                // Special case: If first card is a face card, also allow different ranks
                // (face card effect allows playing any card after it)
                if (isFaceCard(firstCard) && selectedCards.length > 1) {
                    // All subsequent cards should be playable after the face card
                    // Backend will validate this, so we allow it here
                    return true
                }
            }
        }
        
        // Fallback: check if all selected cards are individually playable
        if (props.playableCardIndices && props.playableCardIndices.length > 0) {
            return selectedCards.every(card => 
                props.playableCardIndices?.includes(card.index)
            )
        }
        
        return false
    })

    // Handle draw button click - clear selection before drawing
    function handleDraw() {
        selectedIds.value = []
        emit('draw')
    }

    // Handle one card button click - clear selection before calling oneCard
    function handleOneCard() {
        selectedIds.value = []
        emit('oneCard')
    }

    // Handle playing selected cards
    function handlePlayCards() {
        const indices = getSelectedIndices()
        console.log('handlePlayCards called:', {
            selectedIds: selectedIds.value,
            indices,
            handSize: props.cards?.length,
            cards: props.cards?.map(c => ({ id: c.id, index: c.index, rank: c.rank, shape: c.shape }))
        })
        
        if (indices.length === 0) {
            console.warn('No valid indices to play. Selected IDs:', selectedIds.value)
            return
        }
        
        // Double-check indices are valid
        const handSize = props.cards?.length || 0
        const validIndices = indices.filter(idx => idx >= 0 && idx < handSize)
        
        if (validIndices.length !== indices.length) {
            console.error('Invalid indices detected:', {
                indices,
                validIndices,
                handSize,
                selectedIds: selectedIds.value
            })
            // Clear selection and return - don't send invalid indices
            selectedIds.value = []
            return
        }
        
        emit('playCards', validIndices)
        // Clear selection after playing
        selectedIds.value = []
    }

</script>

<template>
    <div class="player-panel" :class="{ 
        'other-player': !isOwnPanel,
        'player-turn-active': isOwnPanel && isPlayerTurn,
        'ai-turn-active': !isOwnPanel && isPlayerTurn
    }">
        <div class="player-header">
            <div class="header-content">
                <h3>{{ playerName || (isOwnPanel ? 'You' : 'Opponent') }}</h3>
                <span v-if="isOwnPanel && isPlayerTurn" class="turn-badge">
                    <span class="turn-badge-icon">▶</span>
                    <span class="turn-badge-text">YOUR TURN</span>
                </span>
            </div>
            <span v-if="!isOwnPanel" class="hand-count">{{ cards?.length ?? 0 }} cards</span>
        </div>
        
        <!-- Attack warning banner -->
        <div v-if="isOwnPanel && isUnderAttack" class="attack-warning">
            <span class="attack-icon">⚔️</span>
            <span class="attack-text">UNDER ATTACK! You must draw {{ accumulatedDraws }} card(s) or play a defense card!</span>
        </div>
        
        <!-- Face card "one more turn" indicator -->
        <div v-if="isOwnPanel && isPlayerTurn && isFaceCardOnTable" class="face-card-turn-indicator">
            <span class="face-card-icon">👑</span>
            <span class="face-card-text">Face Card Played! Your Turn Continues - Play Another Card!</span>
        </div>
        
        <div class="hand-container">
            <Card
                v-for="card in (cards || [])"
                :key="card.id"
                :card="card"
                :isSelected="isOwnPanel && selectedIds.includes(card.id)"
                :isPlayable="isOwnPanel && isCardPlayable(card)"
                :isDefenseCard="isOwnPanel && isUnderAttack && isDefenseCardPlayable(card)"
                :showBack="!isOwnPanel"
                @select="toggleSelect"
            />
        </div>
        
        <!-- Actions only shown for own panel -->
        <div v-if="isOwnPanel" class="actions">
            <!-- One Card button - shows when player has 1 card left -->
            <button 
                v-if="hasOneCard && isPlayerTurn"
                class="one-card-btn"
                @click="handleOneCard"
            >
                One Card!
            </button>
            
            <!-- Draw Card button - always available during player's turn -->
            <!-- Show outline when under attack and no defense cards available -->
            <button 
                v-if="isPlayerTurn"
                class="draw-btn"
                :class="{ 'defense-mode-draw': shouldHighlightDrawButton }"
                @click="handleDraw"
            >
                Draw Card
            </button>
            
            <!-- Clear Selection button - shows when multiple cards are selected -->
            <button 
                v-if="selectedIds.length > 1 && isPlayerTurn"
                class="clear-btn"
                @click="selectedIds.length = 0"
            >
                Clear Selection
            </button>
            
            <!-- Play Cards button -->
            <!-- Disabled during battle stage if non-defense cards are selected -->
            <button 
                v-if="selectedIds.length > 0 && isPlayerTurn"
                class="play-btn"
                :class="{ 'disabled': !areSelectedCardsValid }"
                :disabled="!areSelectedCardsValid"
                @click="handlePlayCards"
            >
                Drop Card(s)
            </button>
        </div>
    </div>
</template>

<style lang="css" scoped>
.player-panel {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 20px;
    background: #f5f5f5;
    border-radius: 10px;
    border: 2px solid transparent;
    transition: all 0.3s ease;
}

.player-panel.player-turn-active {
    border: 3px solid #3b82f6;
    box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
    background: linear-gradient(135deg, #eff6ff 0%, #f5f5f5 100%);
    animation: player-turn-glow 2s infinite;
}

@keyframes player-turn-glow {
    0%, 100% {
        box-shadow: 0 0 20px rgba(59, 130, 246, 0.4);
        border-color: #3b82f6;
    }
    50% {
        box-shadow: 0 0 30px rgba(59, 130, 246, 0.6);
        border-color: #2563eb;
    }
}

.player-panel.other-player {
    background: #e5e7eb;
    opacity: 0.9;
}

.player-panel.other-player:hover {
    opacity: 1;
}

.player-panel.ai-turn-active {
    border: 3px solid #6b7280;
    box-shadow: 0 0 20px rgba(107, 114, 128, 0.4);
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
    animation: ai-turn-glow 2s infinite;
}

@keyframes ai-turn-glow {
    0%, 100% {
        box-shadow: 0 0 20px rgba(107, 114, 128, 0.4);
        border-color: #6b7280;
    }
    50% {
        box-shadow: 0 0 30px rgba(107, 114, 128, 0.6);
        border-color: #4b5563;
    }
}

.player-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    margin-bottom: 10px;
}

.header-content {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.player-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: bold;
    color: #1f2937;
}

.turn-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    border-radius: 20px;
    font-size: 12px;
    font-weight: bold;
    text-transform: uppercase;
    letter-spacing: 1px;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
    animation: turn-badge-pulse 1.5s infinite;
}

.turn-badge-icon {
    font-size: 10px;
    animation: turn-badge-bounce 1s ease-in-out infinite;
}

.turn-badge-text {
    font-size: 11px;
}

@keyframes turn-badge-pulse {
    0%, 100% {
        transform: scale(1);
        box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
    }
    50% {
        transform: scale(1.05);
        box-shadow: 0 3px 12px rgba(59, 130, 246, 0.6);
    }
}

@keyframes turn-badge-bounce {
    0%, 100% {
        transform: translateX(0);
    }
    50% {
        transform: translateX(3px);
    }
}

.hand-count {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
}

.attack-warning {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    color: white;
    border-radius: 8px;
    font-weight: bold;
    font-size: 16px;
    box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
    animation: attack-pulse 1.5s infinite;
    width: 100%;
    justify-content: center;
}

.attack-icon {
    font-size: 20px;
    animation: attack-swing 1s ease-in-out infinite;
}

.attack-text {
    flex: 1;
    text-align: center;
}

.face-card-turn-indicator {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    color: white;
    border-radius: 8px;
    font-weight: bold;
    font-size: 16px;
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
    animation: face-card-pulse 1.5s infinite;
    width: 100%;
    justify-content: center;
    margin-bottom: 15px;
}

.face-card-icon {
    font-size: 20px;
    animation: face-card-bounce 1s ease-in-out infinite;
}

.face-card-text {
    flex: 1;
    text-align: center;
}

@keyframes face-card-pulse {
    0%, 100% {
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
        transform: scale(1);
    }
    50% {
        box-shadow: 0 6px 16px rgba(16, 185, 129, 0.6);
        transform: scale(1.02);
    }
}

@keyframes face-card-bounce {
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

@keyframes attack-pulse {
    0%, 100% {
        box-shadow: 0 4px 12px rgba(239, 68, 68, 0.4);
        transform: scale(1);
    }
    50% {
        box-shadow: 0 6px 16px rgba(239, 68, 68, 0.6);
        transform: scale(1.02);
    }
}

@keyframes attack-swing {
    0%, 100% {
        transform: rotate(0deg);
    }
    25% {
        transform: rotate(-10deg);
    }
    75% {
        transform: rotate(10deg);
    }
}


.hand-container {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
}

.actions {
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    justify-content: center;
}

.draw-btn,
.one-card-btn,
.play-btn,
.clear-btn {
    padding: 12px 24px;
    font-size: 16px;
    font-weight: bold;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.draw-btn {
    background: #3b82f6;
    color: white;
}

.draw-btn:hover {
    background: #2563eb;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(59, 130, 246, 0.3);
}

/* Draw button in defense mode (under attack, no defense cards available) */
.draw-btn.defense-mode-draw {
    outline: 3px solid #f59e0b;
    outline-offset: -3px;
    box-shadow: 0 0 12px rgba(245, 158, 11, 0.5);
    animation: defense-draw-pulse 2s infinite;
}

@keyframes defense-draw-pulse {
    0%, 100% {
        box-shadow: 0 0 12px rgba(245, 158, 11, 0.5);
    }
    50% {
        box-shadow: 0 0 20px rgba(245, 158, 11, 0.8);
    }
}

.one-card-btn {
    background: #ef4444;
    color: white;
    animation: pulse 2s infinite;
}

.one-card-btn:hover {
    background: #dc2626;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(239, 68, 68, 0.3);
}

.play-btn {
    background: #10b981;
    color: white;
}

.play-btn:hover:not(:disabled) {
    background: #059669;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(16, 185, 129, 0.3);
}

.play-btn.disabled,
.play-btn:disabled {
    background: #9ca3af;
    color: #6b7280;
    cursor: not-allowed;
    opacity: 0.6;
}

.play-btn.disabled:hover,
.play-btn:disabled:hover {
    transform: none;
    box-shadow: none;
}

.clear-btn {
    background: #6b7280;
    color: white;
}

.clear-btn:hover {
    background: #4b5563;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(107, 114, 128, 0.3);
}

@keyframes pulse {
    0%, 100% {
        opacity: 1;
    }
    50% {
        opacity: 0.8;
    }
}
</style> 
