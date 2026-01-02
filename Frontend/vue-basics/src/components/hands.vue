<script setup lang="ts">
    import { ref } from 'vue'
    import Card from './Card.vue'

    interface CardInfo {
        id: number,
        index: number,
        rank: string,
        shape: string
    }

    const props = defineProps<{
        cards: CardInfo[],
        isPlayerTurn?: boolean,
        lastUsedCard?: CardInfo | null,
        accumulatedDraws?: number,
        isInitialTurn?: boolean
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
        return props.cards
            .filter(card => selectedIds.value.has(card.id))
            .map(card => card.index)
            .sort((a, b) => a - b) // Sort for backend (plays cards in order)
    }

    // Check if a card is playable (simplified frontend logic)
    function isCardPlayable(card: CardInfo): boolean {
        // Only show playable indicator during player's turn
        if (!props.isPlayerTurn) {
            return false
        }

        const lastCard = props.lastUsedCard
        const accumulatedDraws = props.accumulatedDraws || 0

        // lastUsedCard should always exist after setup (first card is drawn before dealing)
        if (!lastCard) {
            return false
        }

        // If last card is Joker, any card is playable
        if (lastCard.rank === '-1') {
            return true
        }

        // If under attack (accumulatedDraws > 0), only attack cards can be played
        if (accumulatedDraws > 0) {
            // Attack cards: NumTwo (rank 2), Ace (rank 1), Joker (rank -1)
            const rank = Number.parseInt(card.rank)
            return rank === 2 || rank === 1 || rank === -1
        }

        // Normal matching: same rank OR same shape
        const cardRank = Number.parseInt(card.rank)
        const lastRank = Number.parseInt(lastCard.rank)
        
        // Joker can always be played
        if (cardRank === -1) {
            return true
        }

        // Ace matches any Ace
        if (cardRank === 1 && lastRank === 1) {
            return true
        }

        // Same rank or same shape
        return cardRank === lastRank || card.shape === lastCard.shape
    }
</script>

<template>
    <Card
        v-for="card in cards"
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