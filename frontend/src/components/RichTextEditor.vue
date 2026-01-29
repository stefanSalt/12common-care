<script setup lang="ts">
import { onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import { ElMessage } from 'element-plus'
import { uploadFile } from '../api/file'

type InsertFnType = (url: string, alt: string, href: string) => void

const props = withDefaults(
  defineProps<{
    modelValue: string
    height?: string
    placeholder?: string
  }>(),
  {
    modelValue: '',
    height: '320px',
    placeholder: '请输入内容...',
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
}>()

const editorRef = shallowRef<any>()
const html = ref(props.modelValue ?? '')

watch(
  () => props.modelValue,
  (v) => {
    const next = v ?? ''
    if (next !== html.value) html.value = next
  },
)

watch(html, (v) => emit('update:modelValue', v ?? ''))

const toolbarConfig: Partial<IToolbarConfig> = {}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: props.placeholder,
  MENU_CONF: {},
}

editorConfig.MENU_CONF!['uploadImage'] = {
  async customUpload(file: File, insertFn: InsertFnType) {
    try {
      const uploaded = await uploadFile(file, 'PUBLIC')
      const url = `/api/files/${uploaded.id}/download`
      insertFn(url, uploaded.originalName || 'image', url)
    } catch (e: any) {
      ElMessage.error(e?.message || '图片上传失败')
    }
  },
}

function onCreated(editor: any) {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor) editor.destroy()
})
</script>

<template>
  <div class="rt-root">
    <Toolbar class="rt-toolbar" :editor="editorRef" :defaultConfig="toolbarConfig" mode="default" />
    <Editor
      v-model="html"
      class="rt-editor"
      :defaultConfig="editorConfig"
      mode="default"
      :style="{ height: props.height, overflowY: 'hidden' }"
      @onCreated="onCreated"
    />
  </div>
</template>

<style scoped>
.rt-root {
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.rt-toolbar {
  border-bottom: 1px solid rgba(15, 23, 42, 0.12);
}

.rt-editor {
  overflow-y: hidden;
}
</style>
