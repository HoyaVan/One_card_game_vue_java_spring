<script setup lang="ts">
import { computed } from 'vue'
import Card from './Card.vue'
import type { CardInfo } from '../types/card'

const props = defineProps<{
    lastUsedCard: CardInfo | null
    usedCardPile?: CardInfo[]  // All cards in the used card pile
}>()

// Show the last few cards from the pile, stacked on top of each other
const cardsToShow = computed<CardInfo[]>(() => {
    if (props.usedCardPile && props.usedCardPile.length > 0) {
        // Show last 5 cards (most recent on top)
        return props.usedCardPile.slice(-5).reverse() // Reverse so newest is on top
    }
    if (props.lastUsedCard) {
        return [props.lastUsedCard]
    }
    return []
})
</script>

<template>
    <div class="used-card-pile">
        <div class="pile-label">Used Cards</div>
        <div class="pile-cards">
            <Card
                v-for="(card, index) in cardsToShow"
                :key="card.id"
                :card="card"
                :showBack="false"
                :style="{ 
                    position: 'relative',
                    marginLeft: index > 0 ? '-60px' : '0',
                    zIndex: index + 1,
                    transform: index > 0 ? `rotate(${(index - cardsToShow.length / 2) * 1.5}deg)` : 'none',
                    transition: 'all 0.3s ease'
                }"
            />
        </div>
    </div>
</template>

<style lang='css' scoped>
.used-card-pile {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 20px;
}

.pile-label {
    font-weight: bold;
    font-size: 14px;
    color: #374151;
}

.pile-cards {
    display: flex;
    align-items: center;
    position: relative;
    height: 120px;
    justify-content: center;
}
</style>
