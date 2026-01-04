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

    // Track selected card IDs (not indices, so selection persists even if hand updates)
    const selectedIds = ref<Set<number>>(new Set())

    function toggleSelect(id: number) {
        if (selectedIds.value.has(id)) {
            selectedIds.value.delete(id)
        } else {
            selectedIds.value.add(id)
        }
    }

    // Convert selected IDs back to indices for backend API call
    function getSelectedIndices(): number[] {
        if (!props.cards) return []
        return props.cards
            .filter(card => selectedIds.value.has(card.id))
            .map(card => card.index)
            .sort((a, b) => a - b) // Sort for backend (plays cards in order)
    }

    // Check if a card is playable using backend data
    function isCardPlayable(card: CardInfo): boolean {
        // Only show playable indicator during player's turn
        if (!props.isPlayerTurn) {
            return false
        }

        // Use backend-provided playable card indices
        if (props.playableCardIndices && props.playableCardIndices.length > 0) {
            return props.playableCardIndices.includes(card.index)
        }

        // Fallback: if no playable indices provided, assume none are playable
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
            selectedIds.value.clear()
        }
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
                :isSelected="isOwnPanel && selectedIds.has(card.id)"
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
            
            <!-- Play Cards button -->
            <button 
                v-if="selectedIds.size > 0 && isPlayerTurn"
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
.play-btn {
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

@keyframes pulse {
    0%, 100% {
        opacity: 1;
    }
    50% {
        opacity: 0.8;
    }
}
</style> 
