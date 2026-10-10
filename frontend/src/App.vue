<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { createHealthReport, getApiErrorMessage } from './api/health'

const tasks = ref([
  { id: 1, title: '吃一顿有蔬菜的午饭', note: '12:30 前后', done: true },
  { id: 2, title: '晒十分钟太阳', note: '午后操场见', done: false },
  { id: 3, title: '轻松走跑 3 公里', note: '不看配速也没关系', done: false }
])

const metricDefaults = [
  { id: 'sleep', label: '睡眠', score: 82, tone: 'blue' },
  { id: 'activity', label: '活动', score: 68, tone: 'green' },
  { id: 'diet', label: '饮食', score: 74, tone: 'red' },
  { id: 'mood', label: '心情', score: 88, tone: 'yellow' }
]

function loadMetrics() {
  try {
    const saved = JSON.parse(localStorage.getItem('health-journal-metrics'))
    if (!Array.isArray(saved)) return metricDefaults.map((metric) => ({ ...metric }))

    return metricDefaults.map((metric) => {
      const savedMetric = saved.find((item) => item.id === metric.id)
      const score = Number(savedMetric?.score)
      return {
        ...metric,
        score: Number.isFinite(score) && score >= 0 && score <= 100 ? score : metric.score
      }
    })
  } catch {
    return metricDefaults.map((metric) => ({ ...metric }))
  }
}

const metrics = ref(loadMetrics())

const today = new Intl.DateTimeFormat('zh-CN', {
  timeZone: 'Asia/Shanghai',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  weekday: 'short'
}).formatToParts(new Date())

const datePart = (type) => today.find((part) => part.type === type)?.value || ''
const dateLabel = `${datePart('year')}.${datePart('month')}.${datePart('day')} / ${datePart('weekday')}`

const drawerOpen = ref(false)
const goal = ref('我最近久坐、睡得有点晚，想先用一周改善精神状态和活动量。')
const loading = ref(false)
const errorMessage = ref('')
const report = ref(null)
const toastText = ref('')
const goalInput = ref(null)
let toastTimer

const completedCount = computed(() => tasks.value.filter((task) => task.done).length)
const dailyScore = computed(() => {
  const total = metrics.value.reduce((sum, metric) => sum + metric.score, 0)
  return Math.round(total / metrics.value.length)
})

const scoreFeedback = computed(() => {
  if (dailyScore.value >= 90) {
    return { stamp: ['状态', '很棒'], note: '这份状态，值得给自己一个赞！' }
  }
  if (dailyScore.value >= 75) {
    return { stamp: ['状态', '不错'], note: '下午别忘了站起来！' }
  }
  if (dailyScore.value >= 60) {
    return { stamp: ['状态', '还行'], note: '今天留一点时间出去走走。' }
  }
  if (dailyScore.value >= 40) {
    return { stamp: ['先缓', '一缓'], note: '先照顾睡眠，再慢慢加量。' }
  }
  return { stamp: ['今天', '慢点'], note: '状态不佳时，休息也是计划的一部分。' }
})

function saveMetric(metric) {
  localStorage.setItem(
    'health-journal-metrics',
    JSON.stringify(metrics.value.map(({ id, score }) => ({ id, score })))
  )
  showToast(`${metric.label}评分已记为 ${metric.score} 分`)
}

function showToast(message) {
  toastText.value = message
  window.clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastText.value = ''
  }, 1800)
}

function toggleTask(task) {
  task.done = !task.done
  showToast(task.done ? '今天又完成了一件小事' : '已将这件事放回清单')
}

async function openCoach() {
  drawerOpen.value = true
  document.body.classList.add('drawer-is-open')
  await nextTick()
  goalInput.value?.focus()
}

function closeCoach() {
  drawerOpen.value = false
  document.body.classList.remove('drawer-is-open')
}

async function generatePlan() {
  const message = goal.value.trim()
  if (!message) {
    errorMessage.value = '先写下你最近想改善的事情。'
    goalInput.value?.focus()
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    let conversationId = localStorage.getItem('health-journal-conversation-id')
    if (!conversationId) {
      conversationId = `journal-${Date.now().toString(36)}`
      localStorage.setItem('health-journal-conversation-id', conversationId)
    }
    report.value = await createHealthReport(message, conversationId)
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error)
  } finally {
    loading.value = false
  }
}

function handleEscape(event) {
  if (event.key === 'Escape' && drawerOpen.value) closeCoach()
}

window.addEventListener('keydown', handleEscape)
onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleEscape)
  window.clearTimeout(toastTimer)
  document.body.classList.remove('drawer-is-open')
})
</script>

<template>
  <div class="page-shell">
    <div class="sheet">
      <header class="journal-header">
        <a class="brand" href="#" aria-label="风向健康手账首页">
          <i aria-hidden="true"></i>
          风向健康手账
        </a>

        <div class="header-actions">
          <span class="concept">方案 B · 青春手账</span>
          <button class="coach-button" type="button" @click="openCoach">
            <span class="coach-spark" aria-hidden="true">✦</span>
            问问 AI 教练
          </button>
        </div>
      </header>

      <main class="journal-grid">
        <section class="intro" aria-labelledby="journal-title">
          <span class="date">{{ dateLabel }}</span>
          <h1 id="journal-title">普通的一天，<br /><em>也值得记住。</em></h1>
          <p>今天不和别人比。只记下认真吃饭、好好睡觉，还有愿意出门的自己。</p>
          <div class="scribble">天气：晴　心情：有点期待 ↗</div>
        </section>

        <section class="log" aria-label="今日健康记录">
          <i class="tape" aria-hidden="true"></i>
          <div class="log-head">
            <b>今日训练日志</b>
            <span>连续记录第 18 天</span>
          </div>
          <div class="stamp" :aria-label="scoreFeedback.stamp.join('')">
            {{ scoreFeedback.stamp[0] }}<br />{{ scoreFeedback.stamp[1] }}
          </div>
          <div class="score"><strong>{{ dailyScore }}</strong><span>/ 100 今日状态</span></div>

          <div class="bars">
            <div
              v-for="metric in metrics"
              :key="metric.id"
              class="bar"
              :style="{ '--metric-progress': `${metric.score}%` }"
            >
              <label :for="`metric-${metric.id}`">{{ metric.label }}</label>
              <input
                :id="`metric-${metric.id}`"
                v-model.number="metric.score"
                type="range"
                min="0"
                max="100"
                step="1"
                :class="`metric-range--${metric.tone}`"
                :aria-valuetext="`${metric.label} ${metric.score} 分`"
                @change="saveMetric(metric)"
              />
              <output :for="`metric-${metric.id}`">{{ metric.score }}<small>分</small></output>
            </div>
          </div>

          <div class="memo">
            <span>教练批注：</span>
            <b>{{ scoreFeedback.note }}</b>
          </div>
        </section>

        <section class="right-column" aria-label="今日行动">
          <div>
            <div class="todo-heading-row">
              <h2 class="todo-title">今日三件小事</h2>
              <span>{{ completedCount }}/3</span>
            </div>
            <div class="todo">
              <button
                v-for="task in tasks"
                :key="task.id"
                class="todo-item"
                :class="{ done: task.done }"
                type="button"
                :aria-pressed="task.done"
                @click="toggleTask(task)"
              >
                <i class="check-box" aria-hidden="true"></i>
                <span>
                  <b>{{ task.title }}</b>
                  <small>{{ task.note }}</small>
                </span>
              </button>
            </div>
            <blockquote>“不是非要跑得很快，才算向前。”</blockquote>
          </div>

          <figure class="photo">
            <div class="scene" aria-label="傍晚操场跑步的手绘场景">
              <span class="sun" aria-hidden="true"></span>
              <span class="runner" aria-hidden="true">跑</span>
            </div>
            <figcaption>跑团留言：18:30 老地方集合，等你。</figcaption>
          </figure>
        </section>
      </main>

      <footer class="journal-footer">
        <span>把照顾自己，写进每一个普通日子。</span>
        <span aria-hidden="true">○　○　●　○　○</span>
      </footer>
    </div>

    <Transition name="fade">
      <button v-if="drawerOpen" class="drawer-backdrop" type="button" aria-label="关闭 AI 健康计划" @click="closeCoach"></button>
    </Transition>

    <Transition name="drawer">
      <aside v-if="drawerOpen" class="coach-drawer" aria-labelledby="coach-title" aria-modal="true" role="dialog">
        <div class="drawer-rule" aria-hidden="true"></div>
        <header class="drawer-header">
          <div>
            <span class="pencil-note">给今天一点具体的方向</span>
            <h2 id="coach-title">AI 健康计划</h2>
          </div>
          <button class="close-button" type="button" aria-label="关闭" @click="closeCoach">×</button>
        </header>

        <form class="coach-form" @submit.prevent="generatePlan">
          <label for="health-goal">最近想改善什么？</label>
          <textarea
            id="health-goal"
            ref="goalInput"
            v-model="goal"
            rows="4"
            maxlength="500"
            placeholder="比如：最近总是久坐，希望一周内多活动一点……"
            :disabled="loading"
            @input="errorMessage = ''"
          ></textarea>
          <div class="form-hint">
            <span>不用写姓名等隐私信息</span>
            <span>{{ goal.length }}/500</span>
          </div>
          <button class="generate-button" type="submit" :disabled="loading">
            <span v-if="loading" class="loader" aria-hidden="true"></span>
            {{ loading ? '正在整理计划…' : report ? '重新生成计划' : '生成一周小计划' }}
          </button>
          <p v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</p>
        </form>

        <div v-if="loading" class="loading-note" aria-live="polite">
          <span>AI 教练正在翻阅你的手账</span>
          <i></i><i></i><i></i>
        </div>

        <article v-else-if="report" class="report" aria-live="polite">
          <div class="report-pin" aria-hidden="true"></div>
          <h3>{{ report.title || '健康生活计划' }}</h3>
          <p class="report-summary">{{ report.summary }}</p>

          <section v-if="report.weeklyGoals?.length">
            <h4>这周先做这些</h4>
            <ul class="goal-list">
              <li v-for="item in report.weeklyGoals" :key="item">{{ item }}</li>
            </ul>
          </section>

          <div class="report-grid">
            <section v-if="report.dietSuggestions?.length">
              <h4>吃饭</h4>
              <ul><li v-for="item in report.dietSuggestions" :key="item">{{ item }}</li></ul>
            </section>
            <section v-if="report.exerciseSuggestions?.length">
              <h4>活动</h4>
              <ul><li v-for="item in report.exerciseSuggestions" :key="item">{{ item }}</li></ul>
            </section>
            <section v-if="report.sleepSuggestions?.length">
              <h4>睡眠</h4>
              <ul><li v-for="item in report.sleepSuggestions" :key="item">{{ item }}</li></ul>
            </section>
          </div>

          <section v-if="report.precautions?.length" class="precautions">
            <h4>留意</h4>
            <ul><li v-for="item in report.precautions" :key="item">{{ item }}</li></ul>
          </section>
          <p class="medical-note">计划仅用于一般健康生活参考，不能替代医生诊断或治疗。</p>
        </article>

        <div v-else class="empty-note">
          <span aria-hidden="true">↗</span>
          <p>写下一个真实的小困扰，计划会更适合你。</p>
        </div>
      </aside>
    </Transition>

    <Transition name="toast">
      <div v-if="toastText" class="toast" role="status">{{ toastText }}</div>
    </Transition>
  </div>
</template>
