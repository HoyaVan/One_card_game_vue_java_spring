<script setup>
    import { ref } from 'vue'
    const first_string = "this is the string \n and the space!"
    const count = ref(0)
    const setZero = ref(0)
    const cards = ref(['A', 'K', 'Q', 'J'])
    const showCards = ref(true)
    const selectedCard = ref([])

    function addCardToPlay(card) {
        selectedCard.value.push(card)
    }

</script>

<template>
    <h1>You did it!</h1>
    <p>
        <p> {{ first_string }} </p>
        <p> count: {{ count }} </p>
        <button @click="count++">Increase</button>

        <p v-if="count >= 5" count={{setZero}}>
            High count! More than 5 click!
        </p>

        <ul>
            <li v-for="card in cards" :key="card">
                <button @click="ref(card)">{{ card }}</button>
            </li>
        </ul>
    </p>
</template>

<style scoped></style>
