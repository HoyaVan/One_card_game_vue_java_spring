import type { CardInfo } from '../types/card'

/**
 * Check if a card is an attack card (Ace, 2, Joker)
 * These cards can attack other players or defend against attacks
 */
export function isAttackCard(card: CardInfo): boolean {
    const rank = card.rank.toLowerCase()
    return rank === 'ace' || rank === '2' || rank === 'joker'
}

/**
 * Check if a card is a face card (Jack, Queen, King)
 */
export function isFaceCard(card: CardInfo): boolean {
    const rank = card.rank.toLowerCase()
    return rank === 'jack' || rank === 'queen' || rank === 'king'
}

/**
 * Check if a card can defend against an attack card
 * Defense rules:
 * - Joker can defend against any attack
 * - Ace can defend against Ace (same shape) or 2
 * - 2 can defend against 2 (same rank AND same shape)
 */
export function isDefenseCard(card: CardInfo, attackCard: CardInfo | null): boolean {
    if (!attackCard || !isAttackCard(attackCard)) return false
    
    const cardRank = card.rank.toLowerCase()
    const attackRank = attackCard.rank.toLowerCase()
    
    // Joker can defend against any attack
    if (cardRank === 'joker') return true
    
    // Ace can defend against another Ace regardless of shape, or a 2 of the same shape
    if (cardRank === 'ace') {
        if (attackRank === 'ace') {
            return true
        }
        return card.shape.toLowerCase() === attackCard.shape.toLowerCase()
    }
    
    // 2 can defend against 2 (same rank AND same shape)
    if (cardRank === '2' && attackRank === '2') {
        return card.rank === attackCard.rank && 
               card.shape.toLowerCase() === attackCard.shape.toLowerCase()
    }
    
    return false
}

/**
 * Check if a card is a change shape card (Num 7)
 */
export function isChangeShapeCard(card: CardInfo): boolean {
    const rank = card.rank.toLowerCase()
    return rank === '7'
}
