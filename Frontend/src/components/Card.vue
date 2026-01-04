<script setup lang="ts">
    import type { CardInfo } from '../types/card'

    const props = defineProps<{
        card?: CardInfo,
        isSelected?: boolean,
        isPlayable?: boolean,
        showBack?: boolean  // Show card back (face down) instead of front
    }>() // Receive data from parent

    const emit = defineEmits<{
        select: [id: number]
    }>() // Sends events TO parent

    function onClick() {
        // Only allow clicking if card is playable and not showing back
        if (props.card && !props.showBack && props.isPlayable) {
            emit('select', props.card.id) 
        }
    } // Notifies parent: "I was clicked!"

    function getCardImagePath(card: CardInfo): string {
        // Handle jokers specially - use red_joker.svg or black_joker.svg
        if (card.rank.toLowerCase() === 'joker') {
            // Check if shape explicitly indicates red or black
            const shapeUpper = card.shape.toUpperCase()
            if (shapeUpper === 'RED') {
                return '/SVG-cards/red_joker.svg'
            }
            if (shapeUpper === 'BLACK') {
                return '/SVG-cards/black_joker.svg'
            }
            // Fallback: Use card ID to determine red vs black (alternate based on ID)
            // Even IDs -> red, odd IDs -> black
            const isRed = Math.abs(card.id) % 2 === 0
            return isRed ? '/SVG-cards/red_joker.svg' : '/SVG-cards/black_joker.svg'
        }
        
        // Convert rank and shape to the filename format: {rank}_of_{shape}.svg
        const rank = card.rank.toLowerCase()
        const shape = card.shape.toLowerCase()
        return `/SVG-cards/${rank}_of_${shape}.svg`
    }
</script>

<template>
    <div
        class="card"
        :class="{ 
            selected: isSelected,
            playable: isPlayable && !isSelected && !showBack,
            'non-playable': !isPlayable && !showBack && card,
            'card-back': showBack
        }" 
        @click="onClick"
    > 
        <!-- Card back (face down) -->
        <img 
            v-if="showBack"
            src="/SVG-cards/0_back.png"
            alt="Card back"
            class="card-back-image"
        />
        
        <!-- Card front (face up) -->
        <img 
            v-else-if="card"
            :src="getCardImagePath(card)" 
            :alt="`Card ${card.rank} ${card.shape}`"
        />
    </div>
</template>

<style lang="css" scoped>
.card {
  width: 80px;
  height: 120px;
  border: 1px solid #ccc;
  cursor: default;
  position: relative;
  overflow: hidden;
  transition: all 0.2s ease;
}

.card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Selected card styling - distinct from playable */
.selected {
  border: 3px solid #3b82f6;
  border-radius: 8px;
  transform: translateY(-8px) scale(1.05);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
  z-index: 10;
}

/* Playable card styling - subtle guide outline only */
.playable {
  outline: 2px solid #4ade80;
  outline-offset: -2px;
  box-shadow: 0 0 8px rgba(74, 222, 128, 0.3);
}

/* When card is both playable and selected, only show selected styling */
.selected.playable {
  outline: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.card-back {
  cursor: default;
}

/* Non-playable card styling - faded out and not clickable */
.non-playable {
  opacity: 0.5;
  cursor: default;
  filter: grayscale(30%);
}

.card-back-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style> 
