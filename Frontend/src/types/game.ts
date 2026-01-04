import type { CardInfo } from './card'

export interface GameState {
    playerHand: CardInfo[]
    aiHandSize: number
    lastUsedCard: CardInfo | null
    deckSize: number
    accumulatedDraws: number
    isPlayerTurn: boolean
    isGameOver: boolean
    winner: string | null
    message: string
    playableCardIndices: number[]  // Indices of playable cards
    usedCardPile: CardInfo[]  // All cards in the used card pile (for stacking)
}

export interface GameEvent {
    type: string  // EventType enum from backend
    actor: string  // "PLAYER" or "AI"
    description: string
    cardPlayed?: CardInfo | null
    cardsDrawn?: number | null
    accumulatedDraws?: number | null
}

export interface GameResponse {
    gameState: GameState
    ok: boolean
    message: string
    events?: GameEvent[]
}

