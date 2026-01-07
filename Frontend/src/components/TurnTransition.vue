<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

const props = defineProps<{
    show: boolean
    playerName: string  // "PLAYER" or "AI"
    isBattleSituation?: boolean  // Whether AI is drawing cards during battle (under attack)
    cardsToDraw?: number  // Number of cards AI must draw during battle
}>()

const emit = defineEmits<{
    'transition-complete': []
}>()

const audioRef = ref<HTMLAudioElement | null>(null)

// Generate a simple beep sound using Web Audio API as fallback
function playBeepSound() {
    try {
        const audioContext = new (globalThis.AudioContext || (globalThis as any).webkitAudioContext)()
        const oscillator = audioContext.createOscillator()
        const gainNode = audioContext.createGain()
        
        oscillator.connect(gainNode)
        gainNode.connect(audioContext.destination)
        
        oscillator.frequency.value = 800 // Higher pitch
        oscillator.type = 'sine'
        
        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3)
        
        oscillator.start(audioContext.currentTime)
        oscillator.stop(audioContext.currentTime + 0.3)
    } catch (err) {
        console.log('Web Audio API not available:', err)
    }
}

function handleAudioError() {
    // If audio file fails to load, use beep sound instead
    playBeepSound()
}

// Play sound when transition appears
watch(() => props.show, (newValue) => {
    if (newValue) {
        // Try to play audio file, fallback to beep if it fails
        if (audioRef.value) {
            audioRef.value.play().catch(() => {
                // Audio file not found or can't play - use beep instead
                playBeepSound()
            })
        } else {
            playBeepSound()
        }
        
        // Auto-hide after animation completes
        setTimeout(() => {
            emit('transition-complete')
        }, 2000) // Match animation duration
    }
})

// Preload audio
onMounted(() => {
    if (audioRef.value) {
        audioRef.value.load()
    }
})
</script>

<template>
    <Transition name="turn-transition">
        <div v-if="show" class="turn-overlay">
            <div class="turn-content" :class="{ 'battle-situation': isBattleSituation }">
                <div class="turn-icon">{{ isBattleSituation ? '⚔️' : '🎮' }}</div>
                <h2 class="turn-text">
                    <template v-if="isBattleSituation && playerName === 'AI'">
                        AI Under Attack!
                    </template>
                    <template v-else>
                        {{ playerName === 'PLAYER' ? "Your Turn!" : "AI's Turn" }}
                    </template>
                </h2>
                <div class="turn-subtitle">
                    <template v-if="isBattleSituation && playerName === 'AI'">
                        AI must draw {{ cardsToDraw || 0 }} card(s) or defend!
                    </template>
                    <template v-else>
                        {{ playerName === 'PLAYER' ? "Make your move" : "Thinking..." }}
                    </template>
                </div>
            </div>
            
            <!-- Sound effect -->
            <audio ref="audioRef" preload="auto" @error="handleAudioError">
                <source src="/sounds/turn-change.mp3" type="audio/mpeg" />
                <source src="/sounds/turn-change.ogg" type="audio/ogg" />
            </audio>
        </div>
    </Transition>
</template>

<style scoped>
.turn-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    backdrop-filter: blur(5px);
}

.turn-content {
    text-align: center;
    color: white;
    animation: pulse-glow 2s ease-in-out;
}

.turn-content.battle-situation {
    animation: battle-pulse-glow 2s ease-in-out;
}

.turn-icon {
    font-size: 80px;
    margin-bottom: 20px;
    animation: bounce 1s ease-in-out infinite;
}

.turn-text {
    font-size: 48px;
    font-weight: bold;
    margin: 0;
    text-shadow: 0 0 20px rgba(255, 255, 255, 0.8);
    animation: slide-in 0.5s ease-out;
}

.turn-subtitle {
    font-size: 24px;
    margin-top: 10px;
    opacity: 0.9;
    animation: fade-in 0.5s ease-out 0.3s both;
}

@keyframes pulse-glow {
    0%, 100% {
        transform: scale(1);
        filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
    }
    50% {
        transform: scale(1.05);
        filter: drop-shadow(0 0 30px rgba(255, 255, 255, 0.8));
    }
}

@keyframes battle-pulse-glow {
    0%, 100% {
        transform: scale(1);
        filter: drop-shadow(0 0 15px rgba(239, 68, 68, 0.6));
    }
    50% {
        transform: scale(1.05);
        filter: drop-shadow(0 0 35px rgba(239, 68, 68, 0.9));
    }
}

@keyframes bounce {
    0%, 100% {
        transform: translateY(0);
    }
    50% {
        transform: translateY(-20px);
    }
}

@keyframes slide-in {
    from {
        opacity: 0;
        transform: translateY(-30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes fade-in {
    from {
        opacity: 0;
    }
    to {
        opacity: 0.9;
    }
}

/* Transition animations */
.turn-transition-enter-active {
    transition: opacity 0.3s ease;
}

.turn-transition-leave-active {
    transition: opacity 0.3s ease 1.7s; /* Delay fade out until animation completes */
}

.turn-transition-enter-from,
.turn-transition-leave-to {
    opacity: 0;
}
</style>

