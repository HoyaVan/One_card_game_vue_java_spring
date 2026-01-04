import { ref, type Ref } from 'vue'
import type { GameState, GameResponse } from '../types/game'

const API_BASE_URL = 'http://localhost:8081/games/onecard'

export function useOneCardGame() {
    const gameState: Ref<GameState | null> = ref(null)
    const loading = ref(false)
    const error = ref<string | null>(null)
    const lastEvents: Ref<any[] | null> = ref(null)

    async function fetchGameState(): Promise<GameState | null> {
        loading.value = true
        error.value = null
        try {
            const response = await fetch(`${API_BASE_URL}/state`)
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            const data: GameResponse = await response.json()
            gameState.value = data.gameState
            lastEvents.value = data.events || null
            return data.gameState
        } catch (err) {
            error.value = err instanceof Error ? err.message : 'Failed to fetch game state'
            console.error('Error fetching game state:', err)
            return null
        } finally {
            loading.value = false
        }
    }

    async function setupGame(): Promise<GameState | null> {
        loading.value = true
        error.value = null
        try {
            const response = await fetch(`${API_BASE_URL}/setup`, {
                method: 'POST'
            })
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            const data: GameResponse = await response.json()
            gameState.value = data.gameState
            lastEvents.value = data.events || null
            return data.gameState
        } catch (err) {
            error.value = err instanceof Error ? err.message : 'Failed to setup game'
            console.error('Error setting up game:', err)
            return null
        } finally {
            loading.value = false
        }
    }

    async function playAction(action: string): Promise<GameState | null> {
        loading.value = true
        error.value = null
        try {
            const response = await fetch(`${API_BASE_URL}/play`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ action })
            })
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            const data: GameResponse = await response.json()
            gameState.value = data.gameState
            lastEvents.value = data.events || null
            return data.gameState
        } catch (err) {
            error.value = err instanceof Error ? err.message : 'Failed to play action'
            console.error('Error playing action:', err)
            return null
        } finally {
            loading.value = false
        }
    }

    async function stepGame(): Promise<GameState | null> {
        loading.value = true
        error.value = null
        try {
            const response = await fetch(`${API_BASE_URL}/step`, {
                method: 'POST'
            })
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            const data: GameResponse = await response.json()
            gameState.value = data.gameState
            lastEvents.value = data.events || null
            return data.gameState
        } catch (err) {
            error.value = err instanceof Error ? err.message : 'Failed to step game'
            console.error('Error stepping game:', err)
            return null
        } finally {
            loading.value = false
        }
    }

    async function executeAITurn(): Promise<GameState | null> {
        loading.value = true
        error.value = null
        try {
            const response = await fetch(`${API_BASE_URL}/ai-turn`, {
                method: 'POST'
            })
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`)
            }
            const data: GameResponse = await response.json()
            gameState.value = data.gameState
            lastEvents.value = data.events || null
            return data.gameState
        } catch (err) {
            error.value = err instanceof Error ? err.message : 'Failed to execute AI turn'
            console.error('Error executing AI turn:', err)
            return null
        } finally {
            loading.value = false
        }
    }

    // Helper function to check if a card is playable based on backend data
    function isCardPlayable(cardIndex: number): boolean {
        if (!gameState.value?.isPlayerTurn) {
            return false
        }
        return gameState.value.playableCardIndices.includes(cardIndex)
    }

    return {
        gameState,
        loading,
        error,
        lastEvents,
        fetchGameState,
        setupGame,
        playAction,
        stepGame,
        executeAITurn,
        isCardPlayable
    }
}

