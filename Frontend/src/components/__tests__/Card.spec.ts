import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Card from '../Card.vue'
import type { CardInfo } from '../../types/card'

describe('Card.vue', () => {
  const mockCard: CardInfo = {
    id: 1,
    index: 0,
    rank: 'A',
    shape: 'Hearts'
  }

  describe('onClick function', () => {
    it('should emit select event with card id when card exists and not showing back', async () => {
      const wrapper = mount(Card, {
        props: {
          card: mockCard,
          showBack: false
        }
      })

      await wrapper.trigger('click')

      expect(wrapper.emitted('select')).toBeTruthy()
      expect(wrapper.emitted('select')?.[0]).toEqual([1])
    })

    it('should not emit select event when showBack is true', async () => {
      const wrapper = mount(Card, {
        props: {
          card: mockCard,
          showBack: true
        }
      })

      await wrapper.trigger('click')

      expect(wrapper.emitted('select')).toBeFalsy()
    })

    it('should not emit select event when card is undefined', async () => {
      const wrapper = mount(Card, {
        props: {
          card: undefined,
          showBack: false
        }
      })

      await wrapper.trigger('click')

      expect(wrapper.emitted('select')).toBeFalsy()
    })
  })

  describe('rendering', () => {
    it('should render card back when showBack is true', () => {
      const wrapper = mount(Card, {
        props: {
          card: mockCard,
          showBack: true
        }
      })

      expect(wrapper.find('.card-back-content').exists()).toBe(true)
      expect(wrapper.find('.card-back-pattern').exists()).toBe(true)
    })

    it('should render card front image when showBack is false and card exists', () => {
      const wrapper = mount(Card, {
        props: {
          card: mockCard,
          showBack: false
        }
      })

      const img = wrapper.find('img')
      expect(img.exists()).toBe(true)
      expect(img.attributes('src')).toBe('/cards/A-Hearts.png')
      expect(img.attributes('alt')).toBe('Card A Hearts')
    })

    it('should apply selected class when isSelected is true', () => {
      const wrapper = mount(Card, {
        props: {
          card: mockCard,
          isSelected: true
        }
      })

      expect(wrapper.classes()).toContain('selected')
    })

    it('should apply playable class when isPlayable is true and not selected and not showing back', () => {
      const wrapper = mount(Card, {
        props: {
          card: mockCard,
          isPlayable: true,
          isSelected: false,
          showBack: false
        }
      })

      expect(wrapper.classes()).toContain('playable')
    })

    it('should not apply playable class when isSelected is true', () => {
      const wrapper = mount(Card, {
        props: {
          card: mockCard,
          isPlayable: true,
          isSelected: true,
          showBack: false
        }
      })

      expect(wrapper.classes()).not.toContain('playable')
    })

    it('should not apply playable class when showBack is true', () => {
      const wrapper = mount(Card, {
        props: {
          card: mockCard,
          isPlayable: true,
          isSelected: false,
          showBack: true
        }
      })

      expect(wrapper.classes()).not.toContain('playable')
    })
  })
})

