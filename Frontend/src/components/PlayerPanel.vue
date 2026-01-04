<script setup lang="ts">
    import { ref, computed } from 'vue'
    import Card from './Card.vue'
    import type { CardInfo } from '../types/card'

    const props = withDefaults(defineProps<{
        cards?: CardInfo[]
        isPlayerTurn?: boolean
        lastUsedCard?: CardInfo | null
        accumulatedDraws?: number
        isInitialTurn?: boolean
        playableCardIndices?: number[]  // Backend-provided playable card indices
        isOwnPanel?: boolean  // Whether this is the player's own panel (true) or another player's panel (false)
        playerName?: string  // Name of the player (e.g., "Player", "AI")
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
    function getSelectedIndices(): number[] {
        if (!props.cards) return []
        // Create a map of ID to index for quick lookup
        const idToIndex = new Map<number, number>()
        props.cards.forEach(card => {
            idToIndex.set(card.id, card.index)
        })
        // Return indices in the order they were selected
        return selectedIds.value
            .map(id => idToIndex.get(id))
            .filter((index): index is number => index !== undefined)
    }

    // Helper function to check if a card is a face card (Jack, Queen, King)
    function isFaceCard(card: CardInfo): boolean {
        const rank = card.rank.toLowerCase()
        return rank === 'jack' || rank === 'queen' || rank === 'king'
    }

    // Helper function to check if a card is a normal card (any non-face card)
    function isNormalCard(card: CardInfo): boolean {
        return !isFaceCard(card)
    }

    // Check if a card is playable using backend data and dynamic selection rules
    function isCardPlayable(card: CardInfo): boolean {
        // Only show playable indicator during player's turn
        if (!props.isPlayerTurn) {
            return false
        }

        // If no cards selected, use backend-provided playable card indices
        if (selectedIds.value.length === 0) {
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

        // Rule 1: If first selected card is a face card, allow same shape cards
        if (isFaceCard(firstSelected)) {
            const firstShape = firstSelected.shape.toLowerCase()
            
            // Check if all selected cards so far are face cards of the same shape
            const allFaceCardsSameShape = selectedCards.every(c => 
                isFaceCard(c) && c.shape.toLowerCase() === firstShape
            )

            // Check if a normal card has been selected
            const hasNormalCard = selectedCards.some(c => isNormalCard(c))

            if (hasNormalCard) {
                // Rule 2: After face cards of same shape, if normal card selected, allow same rank normal cards
                const firstNormalCard = selectedCards.find(c => isNormalCard(c))
                if (firstNormalCard) {
                    // Only allow normal cards with same rank
                    return isNormalCard(card) && card.rank === firstNormalCard.rank
                }
            } else if (allFaceCardsSameShape) {
                // Still selecting face cards - must match shape
                if (isFaceCard(card)) {
                    return card.shape.toLowerCase() === firstShape
                }
                // Allow normal cards that match the shape
                if (isNormalCard(card)) {
                    return card.shape.toLowerCase() === firstShape
                }
            }
        } else {
            // Rule 3: If first selected card is a normal card, allow same rank cards
            // BUT only if the first card was playable according to backend rules
            if (isNormalCard(firstSelected)) {
                // First, check if the first selected card was actually playable
                const firstCardWasPlayable = props.playableCardIndices && 
                    props.playableCardIndices.includes(firstSelected.index)
                
                if (firstCardWasPlayable) {
                    // Only allow cards with the same rank if first card was playable
                    return card.rank === firstSelected.rank
                }
                // If first card wasn't playable, don't allow any additional selections
                return false
            }
        }

        // Fallback: use backend-provided playable card indices
        if (props.playableCardIndices && props.playableCardIndices.length > 0) {
            return props.playableCardIndices.includes(card.index)
        }

        return false
    }

    // Check if player has any playable cards (using backend data)
    const hasPlayableCards = computed(() => {
        if (!props.isPlayerTurn || !props.playableCardIndices) {
            return false
        }
        return props.playableCardIndices.length > 0
    })

    // Check if player has only one card left
    const hasOneCard = computed(() => {
        return props.cards?.length === 1
    })

    // Handle draw button click
    function handleDraw() {
        emit('draw')
    }

    // Handle one card button click
    function handleOneCard() {
        emit('oneCard')
    }

    // Handle playing selected cards
    function handlePlayCards() {
        const indices = getSelectedIndices()
        if (indices.length > 0) {
            emit('playCards', indices)
            // Clear selection after playing
            selectedIds.value = []
        }
    }

    // Handle clearing selection
    function handleClearSelection() {
        selectedIds.value = []
    }
</script>

<template>
    <div class="player-panel" :class="{ 'other-player': !isOwnPanel }">
        <div class="player-header">
            <h3>{{ playerName || (isOwnPanel ? 'You' : 'Opponent') }}</h3>
            <span v-if="!isOwnPanel" class="hand-count">{{ cards?.length ?? 0 }} cards</span>
        </div>
        
        <div class="hand-container">
            <Card
                v-for="card in (cards || [])"
                :key="card.id"
                :card="card"
                :isSelected="isOwnPanel && selectedIds.includes(card.id)"
                :isPlayable="isOwnPanel && isCardPlayable(card)"
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
            <button 
                v-if="isPlayerTurn"
                class="draw-btn"
                @click="handleDraw"
            >
                Draw Card
            </button>
            
            <!-- Clear Selection button - shows when multiple cards are selected -->
            <button 
                v-if="selectedIds.length > 1 && isPlayerTurn"
                class="clear-btn"
                @click="handleClearSelection"
            >
                Clear Selection
            </button>
            
            <!-- Play Cards button -->
            <button 
                v-if="selectedIds.length > 0 && isPlayerTurn"
                class="play-btn"
                @click="handlePlayCards"
            >
                Drop Card(s)
            </button>
        </div>
        
        <!-- Show turn indicator for other players -->
        <div v-if="!isOwnPanel && isPlayerTurn" class="turn-indicator">
            {{ playerName || 'Opponent' }}'s Turn
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

.player-panel.other-player {
    background: #e5e7eb;
    opacity: 0.9;
}

.player-panel.other-player:hover {
    opacity: 1;
}

.player-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    margin-bottom: 10px;
}

.player-header h3 {
    margin: 0;
    font-size: 18px;
    font-weight: bold;
    color: #1f2937;
}

.hand-count {
    font-size: 14px;
    color: #6b7280;
    font-weight: 500;
}

.turn-indicator {
    padding: 8px 16px;
    background: #fbbf24;
    color: #78350f;
    border-radius: 6px;
    font-weight: bold;
    font-size: 14px;
    animation: pulse-turn 2s infinite;
}

@keyframes pulse-turn {
    0%, 100% {
        opacity: 1;
        transform: scale(1);
    }
    50% {
        opacity: 0.8;
        transform: scale(1.05);
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

.play-btn:hover {
    background: #059669;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(16, 185, 129, 0.3);
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
