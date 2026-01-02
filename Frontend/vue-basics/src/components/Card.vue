<script setup lang="ts">
    interface CardInfo {
        id: number,
        index: number,
        rank: string,
        shape: string
    }

    const props = defineProps<{
        card: CardInfo,
        isSelected: boolean,
        isPlayable?: boolean
    }>() // Receive data from parent

    const emit = defineEmits<{
        select: [id: number]
    }>() // Sends events TO parent

    function onClick() {
        emit('select', props.card.id) 
    } // Notifies parent: "I was clicked!"
</script>

<template>
    <div
        class="card"
        :class="{ 
            selected: isSelected,
            playable: isPlayable && !isSelected
        }" 
        @click="onClick"
    > 
        <!-- Card image will go here -->
        <img 
            :src="`/cards/${card.rank}-${card.shape}.png`" 
            :alt="`Card ${card.rank} ${card.shape}`"
        />
    </div>
    <!-- :class == v-bind:class, bind this attribute to JavaScript, add class 'selected' when isSelected is true -->
    <!-- @click == v-on:click -->
</template>

<style lang="css" scoped>
.card {
  width: 80px;
  height: 120px;
  border: 1px solid #ccc;
  cursor: pointer;
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
</style> 
