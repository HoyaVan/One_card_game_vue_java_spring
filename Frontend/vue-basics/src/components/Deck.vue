<script setup lang="ts">
    import { ref } from 'vue'

    interface CardData {
        id: number
        value: string
        isSelected: boolean
    }

    const cards = ref<CardData[]>([
        { id: 1, value: 'A', isSelected: false },
        { id: 2, value: 'K', isSelected: false },
        { id: 3, value: 'Q', isSelected: false },
        { id: 4, value: 'J', isSelected: false },
        { id: 5, value: '10', isSelected: false },
        { id: 6, value: '9', isSelected: false },
        { id: 7, value: '8', isSelected: false },
        { id: 8, value: '7', isSelected: false },
        { id: 9, value: '6', isSelected: false },
        { id: 10, value: '5', isSelected: false },
        { id: 11, value: '4', isSelected: false },
        { id: 12, value: '3', isSelected: false },
        { id: 13, value: '2', isSelected: false },
        { id: 14, value: '1', isSelected: false },
        { id: 15, value: 'Joker', isSelected: false },
        { id: 16, value: 'Joker', isSelected: false },
    ])
</script>

<template>

</template>

<style lang='css' scoped>

</style>