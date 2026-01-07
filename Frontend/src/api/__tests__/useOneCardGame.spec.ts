import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useOneCardGame } from '../useOneCardGame'
import type { GameState, GameResponse } from '../../types/game'

// Mock fetch globally
globalThis.fetch = vi.fn() as any

describe('useOneCardGame composable', () => {
  const mockGameState: GameState = {
    playerHand: [
      { id: 1, index: 0, rank: 'A', shape: 'Hearts', isInitialCard: false }
    ],
    aiHandSize: 5,
    lastUsedCard: { id: 2, index: 0, rank: 'K', shape: 'Diamonds', isInitialCard: false },
    deckSize: 30,
    accumulatedDraws: 0,
    isPlayerTurn: true,
    isInitialTurn: false,
    isGameOver: false,
    winner: null,
    message: '',
    playableCardIndices: [0],
    usedCardPile: []
  }

  const mockGameResponse: GameResponse = {
    gameState: mockGameState,
    ok: true,
    message: 'Success',
    events: []
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('fetchGameState function', () => {
    it('should fetch game state from /state endpoint', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => mockGameResponse
      } as Response)

      const { fetchGameState, gameState } = useOneCardGame()
      const result = await fetchGameState()

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/games/onecard/state')
      expect(result).toEqual(mockGameState)
      expect(gameState.value).toEqual(mockGameState)
    })

    it('should set error when fetch fails', async () => {
      vi.mocked(fetch).mockRejectedValueOnce(new Error('Network error'))

      const { fetchGameState, error } = useOneCardGame()
      const result = await fetchGameState()

      expect(result).toBeNull()
      expect(error.value).toBe('Network error')
    })

    it('should set error when response is not ok', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: false,
        status: 500
      } as Response)

      const { fetchGameState, error } = useOneCardGame()
      const result = await fetchGameState()

      expect(result).toBeNull()
      expect(error.value).toContain('HTTP error! status: 500')
    })

    it('should set loading state correctly', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => mockGameResponse
      } as Response)

      const { fetchGameState, loading } = useOneCardGame()
      
      const promise = fetchGameState()
      expect(loading.value).toBe(true)
      
      await promise
      expect(loading.value).toBe(false)
    })
  })

  describe('setupGame function', () => {
    it('should POST to /setup endpoint', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => mockGameResponse
      } as Response)

      const { setupGame, gameState } = useOneCardGame()
      const result = await setupGame()

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/games/onecard/setup', {
        method: 'POST'
      })
      expect(result).toEqual(mockGameState)
      expect(gameState.value).toEqual(mockGameState)
    })

    it('should handle errors correctly', async () => {
      vi.mocked(fetch).mockRejectedValueOnce(new Error('Setup failed'))

      const { setupGame, error } = useOneCardGame()
      const result = await setupGame()

      expect(result).toBeNull()
      expect(error.value).toBe('Setup failed')
    })
  })

  describe('playAction function', () => {
    it('should POST action to /play endpoint', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => mockGameResponse
      } as Response)

      const { playAction, gameState } = useOneCardGame()
      const result = await playAction('1')

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/games/onecard/play', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ action: '1' })
      })
      expect(result).toEqual(mockGameState)
      expect(gameState.value).toEqual(mockGameState)
    })

    it('should update lastEvents when events are present', async () => {
      const responseWithEvents: GameResponse = {
        ...mockGameResponse,
        events: [
          { type: 'TURN_STARTED', actor: 'PLAYER', description: 'Turn started' }
        ]
      }

      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => responseWithEvents
      } as Response)

      const { playAction, lastEvents } = useOneCardGame()
      await playAction('1')

      expect(lastEvents.value).toEqual(responseWithEvents.events)
    })
  })

  describe('executeAITurn function', () => {
    it('should POST to /ai-turn endpoint', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => mockGameResponse
      } as Response)

      const { executeAITurn, gameState } = useOneCardGame()
      const result = await executeAITurn()

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/games/onecard/ai-turn', {
        method: 'POST'
      })
      expect(result).toEqual(mockGameState)
      expect(gameState.value).toEqual(mockGameState)
    })
  })

  describe('stepGame function', () => {
    it('should POST to /step endpoint', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: async () => mockGameResponse
      } as Response)

      const { stepGame, gameState } = useOneCardGame()
      const result = await stepGame()

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/games/onecard/step', {
        method: 'POST'
      })
      expect(result).toEqual(mockGameState)
      expect(gameState.value).toEqual(mockGameState)
    })
  })

  describe('isCardPlayable function', () => {
    it('should return true when card index is in playableCardIndices and isPlayerTurn is true', () => {
      const { isCardPlayable, gameState } = useOneCardGame()
      gameState.value = {
        ...mockGameState,
        isPlayerTurn: true,
        playableCardIndices: [0, 2]
      }

      expect(isCardPlayable(0)).toBe(true)
      expect(isCardPlayable(2)).toBe(true)
      expect(isCardPlayable(1)).toBe(false)
    })

    it('should return false when isPlayerTurn is false', () => {
      const { isCardPlayable, gameState } = useOneCardGame()
      gameState.value = {
        ...mockGameState,
        isPlayerTurn: false,
        playableCardIndices: [0, 2]
      }

      expect(isCardPlayable(0)).toBe(false)
    })

    it('should return false when gameState is null', () => {
      const { isCardPlayable, gameState } = useOneCardGame()
      gameState.value = null

      expect(isCardPlayable(0)).toBe(false)
    })
  })
})

