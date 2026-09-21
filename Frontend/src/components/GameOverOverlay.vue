<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
    winner: string // "PLAYER" or "AI"
    reason?: string
}>()

const isWin = computed(() => props.winner === 'PLAYER')
const resultMessage = computed(() => {
    if (isWin.value) {
        return 'You dropped all your cards!'
    }

    return props.reason === 'PLAYER_MAX_CARDS'
        ? 'You reached the maximum card limit.'
        : 'AI dropped all cards first.'
})
</script>

<template>
    <div class="game-over-overlay" :class="{ 'win': isWin, 'lose': !isWin }">
        <div class="content">
            <div class="icon">{{ isWin ? '🏆' : '💀' }}</div>
            <h1>{{ isWin ? 'VICTORY!' : 'DEFEAT' }}</h1>
            <p>{{ resultMessage }}</p>
            <div class="animation-confetti" v-if="isWin"></div>
        </div>
    </div>
</template>

<style scoped>
.game-over-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    backdrop-filter: blur(5px);
    animation: fadeIn 1s ease-out;
}

.game-over-overlay.win {
    background: rgba(0, 0, 0, 0.5);
}

.game-over-overlay.lose {
    background: rgba(0, 0, 0, 0.7);
}

.content {
    text-align: center;
    color: white;
    padding: 40px;
    border-radius: 20px;
    box-shadow: 0 0 50px rgba(0,0,0,0.5);
    transform: scale(0.8);
    animation: scaleIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

.win .content {
    background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
    border: 4px solid #FFF;
}

.lose .content {
    background: linear-gradient(135deg, #434343 0%, #000000 100%);
    border: 4px solid #FF4444;
}

.icon {
    font-size: 80px;
    margin-bottom: 20px;
    animation: bounce 2s infinite;
}

h1 {
    font-size: 60px;
    margin: 0;
    text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
    font-family: 'Arial Black', sans-serif;
    letter-spacing: 2px;
}

p {
    font-size: 24px;
    margin-top: 10px;
    opacity: 0.9;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

@keyframes scaleIn {
    to { transform: scale(1); }
}

@keyframes bounce {
    0%, 20%, 50%, 80%, 100% {transform: translateY(0);}
    40% {transform: translateY(-30px);}
    60% {transform: translateY(-15px);}
}
</style>
