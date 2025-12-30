<script setup lang="ts">
    import { ref } from 'vue'

    const count = ref<number>(0)
    const cards = ref<string[]>(['A', 'K'])
    const showCards = ref(true)
    function addCards(){
        cards.value.push('Q')
    }
</script>

<template>
    <div>
        <p>Display Count number: {{ count }}</p>
        <div v-if="showCards">
            <p>List Of Cards: </p>
            <ul>
                <li v-for="(card, idx) in cards" :key="idx">
                    {{idx + 1}}. {{ card }}
                </li>
            </ul>
        </div>

        <button @click="count++">Increase the card number manually</button>
        <button @click="showCards = !showCards">Toggle Cards</button>
        <button @click=addCards()>Add Card 'Q'</button>

        <p v-if="count >= 3">High count!</p>
    </div>
</template>

<style>
</style>