<script setup lang="ts">
import { computed } from 'vue'
import Card from './Card.vue'
import type { CardInfo } from '../types/card'
import { isAttackCard } from '../utils/cardHelpers'

const props = defineProps<{
    lastUsedCard: CardInfo | null
    usedCardPile?: CardInfo[]  // All cards in the used card pile
    accumulatedDraws?: number  // Number of accumulated draws (indicates attack state)
    isInitialTurn?: boolean  // Whether this is the initial turn (attack rules don't apply)
}>()

/**
 * Generic helper: check if a card is the initial card
 * 
 * IMPORTANT RULE: The initial card should be treated as a normal matching card,
 * regardless of its actual type. This applies to ALL special cards:
 * - Attack cards (Ace, 2, Joker) - no attack effects
 * - Face cards (Jack, Queen, King) - no special matching rules
 * - Num 7 cards - no shape change effects
 * - Any other functional cards
 * 
 * The initial card is just a starting point for matching, nothing more.
 * 
 * Uses the isInitialCard flag from backend for reliable detection.
 */
function isInitialCard(card: CardInfo): boolean {
    // Use backend-provided flag if available (most reliable)
    if (card.isInitialCard !== undefined) {
        return card.isInitialCard
    }
    
    // Fallback: check if it's the bottom card (for backward compatibility)
    // This handles cases where the flag might not be set
    const totalCards = cardsToShow.value.length
    const cardIndex = cardsToShow.value.findIndex(c => c.id === card.id)
    return cardIndex === totalCards - 1
}

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

// Helper function to check if a card should show attack indicators
// Generic rule: treat the initial card as a normal card, regardless of type
// Only show attack indicators on the top card (most recently played), never on the initial card
// Only show when attack is active (accumulatedDraws > 0)
function shouldShowAttackIndicators(card: CardInfo, index: number): boolean {
    // Only show attack indicators on the top card (most recently played, index 0)
    if (index !== 0) return false
    
    // Never show special card indicators on the initial card
    // This applies generically to all special cards: attack, face, Num 7, etc.
    if (isInitialCard(card)) return false
    
    // Only show indicators if attack is active (accumulatedDraws > 0)
    // If attack is paid off (accumulatedDraws is 0 or undefined), don't show indicators
    if (!props.accumulatedDraws || props.accumulatedDraws <= 0) return false
    
    // Top card (index 0) is an attack card, not the initial card, and attack is active - show indicators
    return isAttackCard(card)
}

// Check if the top card should show attack badge (uses same logic as shouldShowAttackIndicators)
const isTopCardAttack = computed(() => {
    const topCard = cardsToShow.value[0]
    return topCard ? shouldShowAttackIndicators(topCard, 0) : false
})
</script>

<template>
    <div class="used-card-pile">
        <div class="pile-label">
            Used Cards
            <span v-if="isTopCardAttack && accumulatedDraws && accumulatedDraws > 0" class="attack-badge">
                ⚔️ ATTACK
            </span>
        </div>
        <div class="pile-cards">
            <div 
                v-for="(card, index) in cardsToShow"
                :key="card.id"
                class="card-wrapper"
                :class="{ 'attack-card-wrapper': shouldShowAttackIndicators(card, index) }"
                :style="{ 
                    position: 'relative',
                    marginLeft: index > 0 ? '-60px' : '0',
                    zIndex: cardsToShow.length - index,
                    transform: index > 0 ? `rotate(${(index - cardsToShow.length / 2) * 1.5}deg)` : 'none',
                    transition: 'all 0.3s ease'
                }"
            >
                <Card
                    :card="card"
                    :showBack="false"
                />
                <div v-if="shouldShowAttackIndicators(card, index)" class="attack-indicator">
                    ⚔️
                </div>
            </div>
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
    display: flex;
    align-items: center;
    gap: 10px;
}

.attack-badge {
    padding: 4px 8px;
    background: #ef4444;
    color: white;
    border-radius: 4px;
    font-size: 12px;
    font-weight: bold;
    animation: attack-badge-pulse 1.5s infinite;
}

@keyframes attack-badge-pulse {
    0%, 100% {
        opacity: 1;
        transform: scale(1);
    }
    50% {
        opacity: 0.8;
        transform: scale(1.1);
    }
}

.card-wrapper {
    position: relative;
}

.attack-card-wrapper {
    animation: attack-glow 2s infinite;
}

@keyframes attack-glow {
    0%, 100% {
        filter: drop-shadow(0 0 5px rgba(239, 68, 68, 0.5));
    }
    50% {
        filter: drop-shadow(0 0 15px rgba(239, 68, 68, 0.8));
    }
}

.attack-indicator {
    position: absolute;
    top: -8px;
    right: -8px;
    background: #ef4444;
    color: white;
    border-radius: 50%;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: bold;
    box-shadow: 0 2px 8px rgba(239, 68, 68, 0.5);
    z-index: 100;
    animation: attack-indicator-pulse 1s infinite;
}

@keyframes attack-indicator-pulse {
    0%, 100% {
        transform: scale(1);
    }
    50% {
        transform: scale(1.2);
    }
}

.pile-cards {
    display: flex;
    align-items: center;
    position: relative;
    height: 120px;
    justify-content: center;
}

.pile-cards :deep(.card) {
    opacity: 1 !important;
    filter: none !important;
}

.pile-cards :deep(.card img) {
    opacity: 1 !important;
}
</style>
