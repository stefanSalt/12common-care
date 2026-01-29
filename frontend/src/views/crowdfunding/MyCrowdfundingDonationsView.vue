<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { listMyCrowdfundingDonations, type CrowdfundingDonationRecordDto } from '../../api/crowdfunding'

const router = useRouter()

const loading = ref(false)
const rows = ref<CrowdfundingDonationRecordDto[]>([])
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listMyCrowdfundingDonations(page.current, page.size)
    rows.value = data.records ?? []
    page.total = Number(data.total ?? 0)
    page.current = Number(data.current ?? page.current)
    page.size = Number(data.size ?? page.size)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

function goDetail(r: CrowdfundingDonationRecordDto) {
  router.push(`/crowdfunding/${r.projectId}`)
}

const emptyText = computed(() => (loading.value ? '' : '暂无众筹捐款记录'))
</script>

<template>
  <el-card>
    <div style="font-weight: 800">我的众筹捐款</div>
  </el-card>

  <el-card style="margin-top: 12px">
    <el-table :data="rows" v-loading="loading" :empty-text="emptyText" style="width: 100%">
      <el-table-column prop="projectTitle" label="项目" min-width="240" />
      <el-table-column prop="amount" label="金额" width="120" />
      <el-table-column prop="isAnonymous" label="匿名" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.isAnonymous === 1 ? 'info' : 'success'">
            {{ row.isAnonymous === 1 ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="180" />
      <el-table-column prop="createdAt" label="时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="goDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display: flex; justify-content: flex-end; margin-top: 12px">
      <el-pagination
        layout="total, prev, pager, next, sizes"
        :total="page.total"
        :page-size="page.size"
        :current-page="page.current"
        @current-change="
          (p: number) => {
            page.current = p
            load()
          }
        "
        @size-change="
          (s: number) => {
            page.size = s
            page.current = 1
            load()
          }
        "
      />
    </div>
  </el-card>
</template>

