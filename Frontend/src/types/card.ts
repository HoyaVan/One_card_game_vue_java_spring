export interface CardInfo {
    id: number
    index: number
    rank: string
    shape: string
    isInitialCard?: boolean  // Whether this card is the initial card (should be treated as normal card)
}

