<script setup lang="ts">
    const props = defineProps<{
        value: string,
        isFlipped: boolean
    }>()

    const emit = defineEmits<(
        e: 'select', 
        value: string
    ) => void>()

    function onClick() {
        emit('select', props.value)
    }
</script>

<template>
    <div
        class="card"
        :class="{ flipped: isFlipped}" 
        @clcik="onClick"
    > 
        <span v-if="isFlipped">{{ value }}</span>
    </div>
    // :class == v-bind:class, bind this attribute to JavaScript, add class 'flipped' when its true
    // @click == v-on:click
</template>

<style lang="css" scoped>
.card {
  width: 80px;
  height: 120px;
  border: 1px solid #ccc;
  cursor: pointer;
}
.flipped {
  background: #222;
  color: white;
}
</style> 
// scoped - CSS applies only to this component
// cursor: pointer - Mouse cursor changes on hover