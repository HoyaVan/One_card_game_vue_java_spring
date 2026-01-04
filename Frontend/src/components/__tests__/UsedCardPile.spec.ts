import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UsedCardPile from '../UsedCardPile.vue'
import type { CardInfo } from '../../types/card'

describe('UsedCardPile.vue', () => {
  const mockLastCard: CardInfo = {
    id: 1,
    index: 0,
    rank: 'A',
    shape: 'Hearts'
  }

  const mockUsedCardPile: CardInfo[] = [
    { id: 1, index: 0, rank: 'A', shape: 'Hearts' },
    { id: 2, index: 1, rank: '2', shape: 'Spades' },
    { id: 3, index: 2, rank: 'K', shape: 'Diamonds' },
    { id: 4, index: 3, rank: 'Q', shape: 'Clubs' },
    { id: 5, index: 4, rank: 'J', shape: 'Hearts' },
    { id: 6, index: 5, rank: '10', shape: 'Spades' }
  ]

  describe('cardsToShow computed property', () => {
    it('should show last 5 cards from usedCardPile when provided', () => {
      const wrapper = mount(UsedCardPile, {
        props: {
          lastUsedCard: mockLastCard,
          usedCardPile: mockUsedCardPile
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBe(5) // Should show last 5 cards
      
      // Should be reversed (newest on top)
      expect(cards[0].props('card').id).toBe(6) // Most recent
      expect(cards[4].props('card').id).toBe(2) // Oldest of the 5
    })

    it('should show all cards when usedCardPile has less than 5 cards', () => {
      const smallPile = mockUsedCardPile.slice(0, 3)
      const wrapper = mount(UsedCardPile, {
        props: {
          lastUsedCard: mockLastCard,
          usedCardPile: smallPile
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBe(3)
    })

    it('should fallback to lastUsedCard when usedCardPile is not provided', () => {
      const wrapper = mount(UsedCardPile, {
        props: {
          lastUsedCard: mockLastCard
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBe(1)
      expect(cards[0].props('card').id).toBe(1)
    })

    it('should return empty array when neither usedCardPile nor lastUsedCard is provided', () => {
      const wrapper = mount(UsedCardPile, {
        props: {
          lastUsedCard: null
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBe(0)
    })

    it('should show cards face up (showBack=false)', () => {
      const wrapper = mount(UsedCardPile, {
        props: {
          lastUsedCard: mockLastCard,
          usedCardPile: mockUsedCardPile
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      cards.forEach(card => {
        expect(card.props('showBack')).toBe(false)
      })
    })
  })
})

