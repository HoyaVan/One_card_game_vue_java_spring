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
        cards: CardInfo[]
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
</script>

<template>
    <Card
        v-for="card in cards"
        :key="card.id"
        :card="card"
        :isSelected="selectedIds.has(card.id)"
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
