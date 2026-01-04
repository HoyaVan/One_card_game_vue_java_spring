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
        if (props.card && !props.showBack) {
            emit('select', props.card.id) 
        }
    } // Notifies parent: "I was clicked!"
</script>

<template>
    <div
        class="card"
        :class="{ 
            selected: isSelected,
            playable: isPlayable && !isSelected && !showBack,
            'card-back': showBack
        }" 
        @click="onClick"
    > 
        <!-- Card back (face down) -->
        <div v-if="showBack" class="card-back-content">
            <div class="card-back-pattern"></div>
        </div>
        
        <!-- Card front (face up) -->
        <img 
            v-else-if="card"
            :src="`/cards/${card.rank}-${card.shape}.png`" 
            :alt="`Card ${card.rank} ${card.shape}`"
        />
    </div>
</template>

<style lang="css" scoped>
.card {
  width: 80px;
  height: 120px;
  border: 1px solid #ccc;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.selected {
  background: #222;
  color: white;
}

.playable {
  outline: 3px solid #4ade80;
  outline-offset: -3px;
  box-shadow: 0 0 10px rgba(74, 222, 128, 0.5);
}

.card-back {
  background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
  border: 2px solid #1e40af;
  cursor: default;
}

.card-back-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-back-pattern {
  width: 60px;
  height: 90px;
  background: repeating-linear-gradient(
    45deg,
    rgba(255, 255, 255, 0.1) 0px,
    rgba(255, 255, 255, 0.1) 10px,
    transparent 10px,
    transparent 20px
  );
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
}
</style> 
