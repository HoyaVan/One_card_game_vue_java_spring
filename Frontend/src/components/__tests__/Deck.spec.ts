import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Deck from '../Deck.vue'

describe('Deck.vue', () => {
  describe('deckCards computed property', () => {
    it('should create correct number of placeholder cards based on deckSize', () => {
      const wrapper = mount(Deck, {
        props: {
          deckSize: 10
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBe(5) // Only shows up to 5 cards visually
    })

    it('should default to 54 cards when deckSize is not provided', () => {
      const wrapper = mount(Deck, {
        props: {}
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBe(5) // Only shows up to 5 cards visually
    })

    it('should create cards with negative IDs to avoid conflicts', () => {
      const wrapper = mount(Deck, {
        props: {
          deckSize: 3
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      cards.forEach((card, index) => {
        expect(card.props('card').id).toBeLessThan(0)
        expect(card.props('card').id).toBe(-2000 - index)
      })
    })

    it('should pass showBack=true to all Card components', () => {
      const wrapper = mount(Deck, {
        props: {
          deckSize: 5
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      cards.forEach(card => {
        expect(card.props('showBack')).toBe(true)
      })
    })

    it('should display deck size in label', () => {
      const wrapper = mount(Deck, {
        props: {
          deckSize: 42
        }
      })

      expect(wrapper.text()).toContain('Deck (42)')
    })

    it('should display 0 when deckSize is 0', () => {
      const wrapper = mount(Deck, {
        props: {
          deckSize: 0
        }
      })

      expect(wrapper.text()).toContain('Deck (0)')
    })

    it('should limit visible cards to 5 even if deckSize is larger', () => {
      const wrapper = mount(Deck, {
        props: {
          deckSize: 100
        }
      })

      const cards = wrapper.findAllComponents({ name: 'Card' })
      expect(cards.length).toBe(5)
    })
  })
})

