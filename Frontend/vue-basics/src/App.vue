<script setup lang="ts">
    import {ref} from 'vue'
    import Card from './components/Card.vue'

    const cards = ref<string[]>(['1', '2', '3', '4', '5',
                                '6', '7', '8', '9', '10', 
                                'A', 'J', 'Q', 'K', 'Joker'])

    function onCardSelected(card: string) {
        console.log('Selected:', card)
    }
</script>

<template>
    <Card 
        v-for="card in cards" 
        :key="card" 
        :value="card"
        @select="onCardSelected"
    />
</template>

<style>

</style>