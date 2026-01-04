<script setup lang="ts">
    import { computed } from 'vue'
    import Card from './Card.vue'
    import type { CardInfo } from '../types/card'

    const props = defineProps<{
        deckSize?: number  // Number of cards remaining in deck
    }>()

    // Create placeholder cards for deck (face down)
    const deckCards = computed<CardInfo[]>(() => {
        const size = props.deckSize ?? 54 // Default to full deck if not provided
        const cards: CardInfo[] = []
        for (let i = 0; i < size; i++) {
            cards.push({
                id: -2000 - i, // Negative IDs to avoid conflicts
                index: i,
                rank: '',
                shape: ''
            })
        }
        return cards
    })
</script>

<template>
    <div class="deck">
        <div class="deck-cards">
            <!-- Show only a few cards stacked to represent the deck -->
            <Card
                v-for="(card, index) in deckCards.slice(0, Math.min(5, deckCards.length))"
                :key="card.id"
                :card="card"
                :showBack="true"
                :style="{ 
                    position: 'relative',
                    marginLeft: index > 0 ? '-60px' : '0',
                    zIndex: deckCards.length - index
                }"
            />
        </div>
    </div>
</template>

<style lang='css' scoped>
    .deck {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;
        padding: 20px;
    }

    .deck-label {
        font-weight: bold;
        font-size: 14px;
        color: #374151;
    }

    .deck-cards {
        display: flex;
        align-items: center;
        position: relative;
        height: 120px;
    }
</style>