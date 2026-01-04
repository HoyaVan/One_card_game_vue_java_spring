<script setup lang="ts">
    import { ref } from 'vue'
    import Card from './Card.vue'
    import type { CardInfo } from '../types/card'

    const props = withDefaults(defineProps<{
        cards?: CardInfo[],
        isPlayerTurn?: boolean,
        lastUsedCard?: CardInfo | null,
        accumulatedDraws?: number,
        isInitialTurn?: boolean,
        playableCardIndices?: number[]  // Backend-provided playable card indices
    }>(), {
        cards: () => []
    })

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
</script>

<template>
    <Card
        v-for="card in (cards || [])"
        :key="card.id"
        :card="card"
        :isSelected="selectedIds.has(card.id)"
        :isPlayable="isCardPlayable(card)"
        @select="toggleSelect"
    />
</template>

<style lang="css" scoped>
.card {
  width: 80px;
  height: 120px;
  border: 1px solid #ccc;
  cursor: pointer;
}
.flipped {
  background: #222;
  color: white;
}
</style> 
// scoped - CSS applies only to this component
// cursor: pointer - Mouse cursor changes on hover