<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h1>数据仪表盘</h1>
      <p>实时监控系统运营数据</p>
    </div>

    <el-row :gutter="20">
      <!-- 每日销量趋势图 -->
      <el-col :span="14">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近30天销量趋势</span>
            </div>
          </template>
          <div ref="dailySalesChart" style="width: 100%; height: 400px;"></div>
        </el-card>
      </el-col>

      <!-- 今日销量Top8 -->
      <el-col :span="10">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>今日销量Top8</span>
            </div>
          </template>
          <div ref="topBooksChart" style="width: 100%; height: 400px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { orderAPI } from '@/api'

const dailySalesChart = ref(null)
const topBooksChart = ref(null)

const loadStatistics = async () => {
  try {
    const res = await orderAPI.getStatistics()
    
    await nextTick()
    
    // 渲染每日销量图表
    if (dailySalesChart.value) {
      const chart1 = echarts.init(dailySalesChart.value)
      chart1.setOption({
        title: {
          text: '每日销量统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          top: '15%',
          bottom: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: res.data.dailySales?.dates || [],
          axisLabel: {
            rotate: 45,
            fontSize: 11
          }
        },
        yAxis: {
          type: 'value',
          name: '销量',
          nameTextStyle: {
            fontSize: 12
          }
        },
        series: [{
          name: '销量',
          type: 'line',
          smooth: true,
          data: res.data.dailySales?.sales || [],
          itemStyle: {
            color: '#7c3aed'
          },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [{
                offset: 0,
                color: 'rgba(124, 58, 237, 0.3)'
              }, {
                offset: 1,
                color: 'rgba(124, 58, 237, 0.05)'
              }]
            }
          }
        }]
      })
    }

    // 渲染今日Top8图表
    if (topBooksChart.value) {
      const chart2 = echarts.init(topBooksChart.value)
      const topBooks = res.data.topBooks || []
      chart2.setOption({
        title: {
          text: '今日热销书籍',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            const data = params[0]
            return `${data.name}<br/>销量: ${data.value}本`
          }
        },
        grid: {
          left: '5%',
          right: '10%',
          top: '15%',
          bottom: '5%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          position: 'top'
        },
        yAxis: {
          type: 'category',
          data: topBooks.map(b => b.bookName).reverse(),
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: {
            margin: 16,
            fontSize: 12,
            color: '#666',
            overflow: 'truncate',
            width: 120,
            ellipsis: '...'
          }
        },
        series: [{
          name: '销量',
          type: 'bar',
          data: topBooks.map(b => b.sales).reverse(),
          itemStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 1,
              y2: 0,
              colorStops: [{
                offset: 0,
                color: '#7c3aed'
              }, {
                offset: 1,
                color: '#a78bfa'
              }]
            }
          },
          barWidth: '60%',
          label: {
            show: true,
            position: 'right',
            fontSize: 12,
            color: '#666'
          }
        }]
      })
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
</style>
