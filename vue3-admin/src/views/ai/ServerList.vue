<!-- TODO: i18n - migrate all hardcoded labels/buttons to t('ai.*') keys (namespace not yet added to locale files) -->
<template>
  <div class="server-management">
    <el-row :gutter="20">
      <!-- Server List Panel -->
      <el-col :span="8">
        <el-card class="server-list-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Server List</span>
              <el-button type="primary" size="small" @click="showServerDialog(null)">Add</el-button>
            </div>
          </template>

          <!-- Search -->
          <el-form :model="queryParams" inline class="search-form">
            <el-form-item label="Keyword">
              <el-input v-model="queryParams.keyword" placeholder="Name/Host" clearable @keyup.enter="loadServers" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadServers">Search</el-button>
            </el-form-item>
          </el-form>

          <!-- Server List -->
          <div class="server-list">
            <div
              v-for="server in serverList"
              :key="server.id"
              :class="['server-item', { active: selectedServer?.id === server.id }]"
              @click="selectServer(server)"
            >
              <div class="server-info">
                <div class="server-name">
                  <el-icon><Monitor /></el-icon>
                  {{ server.name }}
                </div>
                <div class="server-host">{{ server.host }}:{{ server.port || 22 }}</div>
                <div class="server-meta">
                  <el-tag :type="getStatusType(server.status)" size="small">
                    {{ getStatusLabel(server.status) }}
                  </el-tag>
                  <el-tag v-if="server.serverGroup" size="small">{{ server.serverGroup }}</el-tag>
                </div>
              </div>
            </div>
            <el-empty v-if="serverList.length === 0" description="No servers" />
          </div>

          <!-- Pagination -->
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, prev, pager, next"
            @size-change="loadServers"
            @current-change="loadServers"
            small
            class="server-pagination"
          />
        </el-card>
      </el-col>

      <!-- Terminal Panel -->
      <el-col :span="16">
        <el-card class="terminal-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>
                Terminal
                <span v-if="selectedServer"> - {{ selectedServer.name }}</span>
              </span>
              <div class="terminal-actions">
                <el-button v-if="selectedServer" size="small" @click="testConnection">Test Connection</el-button>
                <el-button v-if="selectedServer" size="small" type="primary" @click="connectServer">
                  {{ selectedServer.status === 1 ? 'Reconnect' : 'Connect' }}
                </el-button>
                <el-button v-if="selectedServer" size="small" type="danger" @click="disconnectServer">Disconnect</el-button>
                <el-button v-if="wsConnected" size="small" type="success" disabled>
                  <el-icon><Connection /></el-icon> Connected
                </el-button>
              </div>
            </div>
          </template>

          <!-- AI Command Input -->
          <div v-if="selectedServer" class="ai-command-section">
            <el-input
              v-model="aiPrompt"
              placeholder="Describe what you want to do in natural language, e.g., 'show system info' or 'check docker status'"
              class="ai-input"
              @keyup.enter="generateAiCommand"
            >
              <template #append>
                <el-button @click="generateAiCommand" :loading="aiLoading">
                  <el-icon v-if="!aiLoading"><MagicStick /></el-icon>
                  AI Generate
                </el-button>
              </template>
            </el-input>

            <!-- Command Templates -->
            <div class="command-templates">
              <span class="template-label">Quick commands:</span>
              <el-button
                v-for="template in commandTemplates"
                :key="template.id"
                size="small"
                @click="executeTemplate(template)"
                class="template-btn"
              >
                {{ template.name }}
              </el-button>
            </div>
          </div>

          <!-- Terminal Output -->
          <div v-if="selectedServer" class="terminal-container" ref="terminalContainer">
            <div ref="terminalRef" class="xterm-container"></div>
          </div>

          <!-- Command Input (fallback when WebSocket not available) -->
          <div v-if="selectedServer && !wsConnected" class="terminal-input-container">
            <span class="prompt">$</span>
            <input
              ref="commandInput"
              v-model="currentCommand"
              class="terminal-input"
              placeholder="Enter command..."
              @keyup.enter="executeCurrentCommand"
            />
            <el-button type="primary" size="small" @click="executeCurrentCommand" :loading="executing">
              Execute
            </el-button>
          </div>

          <el-empty v-if="!selectedServer" description="Select a server to start" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Server Dialog -->
    <el-dialog v-model="serverDialogVisible" :title="serverForm.id ? 'Edit Server' : 'Add Server'" width="600px">
      <el-form :model="serverForm" label-width="120px">
        <el-form-item label="Server Name" required>
          <el-input v-model="serverForm.name" placeholder="e.g., Production Server 1" />
        </el-form-item>
        <el-form-item label="Host IP" required>
          <el-input v-model="serverForm.host" placeholder="e.g., 192.168.1.100" />
        </el-form-item>
        <el-form-item label="SSH Port">
          <el-input-number v-model="serverForm.port" :min="1" :max="65535" />
        </el-form-item>
        <el-form-item label="Username" required>
          <el-input v-model="serverForm.username" placeholder="e.g., root" />
        </el-form-item>
        <el-form-item label="Auth Type">
          <el-radio-group v-model="serverForm.authType">
            <el-radio label="password">Password</el-radio>
            <el-radio label="key">Private Key</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="serverForm.authType === 'password'" label="Password">
          <el-input v-model="serverForm.password" type="password" show-password placeholder="SSH password" />
        </el-form-item>
        <el-form-item v-if="serverForm.authType === 'key'" label="Private Key">
          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="handleKeyChange"
            accept=".pem,.key"
          >
            <el-button>Select Key File</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="serverForm.authType === 'key'" label="Passphrase">
          <el-input v-model="serverForm.passphrase" type="password" show-password placeholder="Key passphrase (optional)" />
        </el-form-item>
        <el-form-item label="OS Type">
          <el-select v-model="serverForm.osType" placeholder="Select OS" clearable style="width: 100%">
            <el-option label="Ubuntu" value="Ubuntu" />
            <el-option label="CentOS" value="CentOS" />
            <el-option label="RHEL" value="RHEL" />
            <el-option label="Debian" value="Debian" />
            <el-option label="Alpine" value="Alpine" />
            <el-option label="Other" value="Linux" />
          </el-select>
        </el-form-item>
        <el-form-item label="Server Group">
          <el-input v-model="serverForm.serverGroup" placeholder="e.g., Production, Development" />
        </el-form-item>
        <el-form-item label="Tags">
          <el-input v-model="serverForm.tags" placeholder="Comma separated tags" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="serverForm.description" type="textarea" placeholder="Server description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="serverDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveServer" :loading="serverSaving">Save</el-button>
        <el-button type="success" @click="testAndSave" :loading="serverSaving">Test & Save</el-button>
      </template>
    </el-dialog>

    <!-- History Dialog -->
    <el-dialog v-model="historyDialogVisible" title="Command History" width="900px">
      <el-table :data="historyList" stripe>
        <el-table-column prop="createTime" label="Time" width="180" />
        <el-table-column prop="serverName" label="Server" width="150" />
        <el-table-column prop="command" label="Command" min-width="200">
          <template #default="{ row }">
            <code>{{ row.command }}</code>
            <el-tag v-if="row.aiPrompt" size="small" type="warning">AI</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exitCode" label="Exit Code" width="100">
          <template #default="{ row }">
            <el-tag :type="row.exitCode === 0 ? 'success' : 'danger'" size="small">
              {{ row.exitCode ?? 'N/A' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="Duration" width="100">
          <template #default="{ row }">
            {{ row.duration ? row.duration + 'ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="showOutput(row)">Output</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="historyParams.pageNum"
        v-model:page-size="historyParams.pageSize"
        :total="historyTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadHistory"
        @current-change="loadHistory"
        style="margin-top: 20px"
      />
    </el-dialog>

    <!-- Output Dialog -->
    <el-dialog v-model="outputDialogVisible" title="Command Output" width="800px">
      <pre class="output-pre">{{ currentOutput }}</pre>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, MagicStick, Connection } from '@element-plus/icons-vue'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import 'xterm/css/xterm.css'
import {
  serverPage,
  serverDetail,
  serverCreate,
  serverUpdate,
  serverDelete,
  serverConnect,
  serverDisconnect,
  serverTestConnection,
  executeCommand,
  executeAiCommand,
  commandHistory,
  aiGenerateCommand,
  aiGetTemplates,
  importPrivateKey,
  type Server,
  type CommandHistory,
  type CommandTemplate
} from '@/api/ai/server'

const loading = ref(false)
const serverList = ref<Server[]>([])
const total = ref(0)
const selectedServer = ref<Server | null>(null)
const commandHistoryList = ref<CommandHistory[]>([])

// WebSocket
const wsConnected = ref(false)
const ws = ref<WebSocket | null>(null)
const terminal = ref<Terminal | null>(null)
const fitAddon = ref<FitAddon | null>(null)

// Query params
const queryParams = reactive({
  keyword: '',
  serverGroup: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

// Server dialog
const serverDialogVisible = ref(false)
const serverForm = reactive<Server>({
  id: undefined,
  name: '',
  host: '',
  port: 22,
  username: '',
  authType: 'password',
  password: '',
  privateKey: '',
  passphrase: '',
  osType: '',
  serverGroup: 'default',
  tags: '',
  description: ''
})
const serverSaving = ref(false)

// Terminal
const terminalOutput = ref<HTMLElement | null>(null)
const terminalContainer = ref<HTMLElement | null>(null)
const commandInput = ref<HTMLInputElement | null>(null)
const currentCommand = ref('')
const executing = ref(false)
const sessionId = ref('')

// AI
const aiPrompt = ref('')
const aiLoading = ref(false)
const commandTemplates = ref<CommandTemplate[]>([])

// Command history display
const commandHistory = ref<Array<{
  command: string
  output: string
  exitCode?: number
  duration?: number
  isAi: boolean
}>>([])

// History dialog
const historyDialogVisible = ref(false)
const historyList = ref<CommandHistory[]>([])
const historyTotal = ref(0)
const historyParams = reactive({
  serverId: undefined as number | undefined,
  sessionId: '',
  pageNum: 1,
  pageSize: 20
})

// Output dialog
const outputDialogVisible = ref(false)
const currentOutput = ref('')

const loadServers = async () => {
  loading.value = true
  try {
    const result = await serverPage(queryParams)
    if (result.code === '0000') {
      serverList.value = result.data.list || []
      total.value = result.data.total || 0
    }
  } catch {
    ElMessage.error('Failed to load servers')
  } finally {
    loading.value = false
  }
}

const selectServer = async (server: Server) => {
  selectedServer.value = server
  sessionId.value = `session-${Date.now()}`
  commandHistory.value = []

  // Initialize WebSocket terminal
  await initTerminal()
  await connectWebSocket()
}

const initTerminal = async () => {
  if (terminal.value) {
    terminal.value.dispose()
  }

  await nextTick()
  const terminalRef = window.document.querySelector('.xterm-container') as HTMLElement
  if (!terminalRef) return

  terminal.value = new Terminal({
    cursorBlink: true,
    fontSize: 14,
    fontFamily: 'Courier New, monospace',
    theme: {
      background: '#1e1e1e',
      foreground: '#d4d4d4'
    }
  })

  fitAddon.value = new FitAddon()
  terminal.value.loadAddon(fitAddon.value)
  terminal.value.open(terminalRef)
  fitAddon.value.fit()

  // Handle terminal input
  terminal.value.onData(data => {
    if (ws.value && wsConnected.value) {
      ws.value.send(JSON.stringify({ type: 'input', data }))
    }
  })
}

const connectWebSocket = async () => {
  if (!selectedServer.value?.id) return

  const wsUrl = `ws://${window.location.host}/ws/ssh`
  const socket = new WebSocket(wsUrl)

  socket.onopen = () => {
    wsConnected.value = true
    ws.value = socket

    // Create session for selected server
    socket.send(JSON.stringify({
      type: 'create_session',
      serverId: selectedServer.value?.id
    }))

    terminal.value?.write('\r\n\x1b[32mConnected to server\x1b[0m\r\n\r\n$ ')
  }

  socket.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.type === 'session_created') {
        terminal.value?.write(`\r\n\x1b[32mSession created for: ${msg.serverName}\x1b[0m\r\n\r\n$ `)
      } else if (msg.type === 'output') {
        terminal.value?.write(`\r\n${msg.output}\r\n$ `)
      } else if (msg.type === 'error') {
        terminal.value?.write(`\r\n\x1b[31mError: ${msg.message}\x1b[0m\r\n$ `)
      }
    } catch {
      terminal.value?.write(event.data)
    }
  }

  socket.onclose = () => {
    wsConnected.value = false
    terminal.value?.write('\r\n\x1b[33mConnection closed\x1b[0m\r\n')
  }

  socket.onerror = () => {
    wsConnected.value = false
    terminal.value?.write('\r\n\x1b[31mConnection error\x1b[0m\r\n')
  }
}

const showServerDialog = (server: Server | null) => {
  if (server) {
    Object.assign(serverForm, server)
  } else {
    Object.assign(serverForm, {
      id: undefined,
      name: '',
      host: '',
      port: 22,
      username: '',
      authType: 'password',
      password: '',
      privateKey: '',
      passphrase: '',
      osType: '',
      serverGroup: 'default',
      tags: '',
      description: ''
    })
  }
  serverDialogVisible.value = true
}

const handleKeyChange = async (file: any) => {
  try {
    const result = await importPrivateKey(file.raw)
    if (result.code === '0000') {
      serverForm.privateKey = result.data
      ElMessage.success('Private key imported successfully')
    }
  } catch {
    ElMessage.error('Failed to import key')
  }
}

const saveServer = async () => {
  if (!serverForm.name || !serverForm.host || !serverForm.username) {
    ElMessage.warning('Please fill in required fields')
    return
  }

  serverSaving.value = true
  try {
    if (serverForm.id) {
      await serverUpdate(serverForm)
      ElMessage.success('Server updated')
    } else {
      await serverCreate(serverForm)
      ElMessage.success('Server created')
    }
    serverDialogVisible.value = false
    loadServers()
  } catch {
    ElMessage.error('Failed to save server')
  } finally {
    serverSaving.value = false
  }
}

const testAndSave = async () => {
  if (!serverForm.name || !serverForm.host || !serverForm.username) {
    ElMessage.warning('Please fill in required fields')
    return
  }

  serverSaving.value = true
  try {
    // Create first
    if (!serverForm.id) {
      const result = await serverCreate(serverForm)
      if (result.code !== '0000') {
        ElMessage.error(result.msg || 'Failed to create server')
        return
      }
      serverForm.id = result.data
    }

    // Test connection
    const testResult = await serverTestConnection(serverForm.id!)
    if (testResult.code === '0000' && testResult.data) {
      ElMessage.success('Connection successful')
      serverDialogVisible.value = false
      loadServers()
    } else {
      ElMessage.warning('Connection failed, server saved but not connected')
      serverDialogVisible.value = false
      loadServers()
    }
  } catch {
    ElMessage.error('Failed to test connection')
  } finally {
    serverSaving.value = false
  }
}

const connectServer = async () => {
  if (!selectedServer.value?.id) return

  try {
    const result = await serverConnect(selectedServer.value.id)
    if (result.code === '0000') {
      selectedServer.value.status = result.data ? 1 : 2
      ElMessage.success(result.data ? 'Connected' : 'Connection failed')
      loadServers()
    }
  } catch {
    ElMessage.error('Failed to connect')
  }
}

const disconnectServer = async () => {
  if (!selectedServer.value?.id) return

  try {
    await serverDisconnect(selectedServer.value.id)
    selectedServer.value.status = 0
    ElMessage.success('Disconnected')
    loadServers()
  } catch {
    ElMessage.error('Failed to disconnect')
  }
}

const testConnection = async () => {
  if (!selectedServer.value?.id) return

  try {
    const result = await serverTestConnection(selectedServer.value.id)
    if (result.code === '0000') {
      ElMessage.success(result.data ? 'Connection successful' : 'Connection failed')
    }
  } catch {
    ElMessage.error('Failed to test connection')
  }
}

const executeCurrentCommand = async () => {
  if (!selectedServer.value?.id || !currentCommand.value.trim()) return

  const cmd = currentCommand.value.trim()
  currentCommand.value = ''

  // Add to display
  commandHistory.value.push({
    command: cmd,
    output: '',
    isAi: false
  })

  await scrollToBottom()
  executing.value = true

  try {
    const result = await executeCommand({
      serverId: selectedServer.value.id,
      command: cmd,
      sessionId: sessionId.value
    })

    if (result.code === '0000') {
      const lastItem = commandHistory.value[commandHistory.value.length - 1]
      lastItem.output = result.data.output || ''
      lastItem.exitCode = result.data.exitCode
      lastItem.duration = result.data.duration
    } else {
      const lastItem = commandHistory.value[commandHistory.value.length - 1]
      lastItem.output = result.msg || 'Execution failed'
      lastItem.exitCode = -1
    }
  } catch (e: any) {
    const lastItem = commandHistory.value[commandHistory.value.length - 1]
    lastItem.output = e.message || 'Execution failed'
    lastItem.exitCode = -1
  } finally {
    executing.value = false
    await nextTick()
    await scrollToBottom()
  }
}

const generateAiCommand = async () => {
  if (!selectedServer.value?.id || !aiPrompt.value.trim()) return

  aiLoading.value = true

  try {
    // First validate command
    const validation = await aiGetTemplates()
    if (validation.code !== '0000') {
      ElMessage.error('Failed to load templates')
      return
    }

    // Generate command using AI
    const result = await aiGenerateCommand(selectedServer.value.id, aiPrompt.value)
    if (result.code === '0000') {
      const aiCommand = result.data.command

      // Show generated command
      ElMessage.info(`AI generated: ${aiCommand}`)

      // Execute the command
      commandHistory.value.push({
        command: aiCommand,
        output: '',
        isAi: true
      })

      await scrollToBottom()

      const execResult = await executeCommand({
        serverId: selectedServer.value.id,
        command: aiCommand,
        sessionId: sessionId.value
      })

      if (execResult.code === '0000') {
        const lastItem = commandHistory.value[commandHistory.value.length - 1]
        lastItem.output = execResult.data.output || ''
        lastItem.exitCode = execResult.data.exitCode
        lastItem.duration = execResult.data.duration
      }

      aiPrompt.value = ''
    } else {
      ElMessage.warning(result.msg || 'Failed to generate command')
    }
  } catch (e: any) {
    ElMessage.error(e.message || 'Failed to generate command')
  } finally {
    aiLoading.value = false
    await nextTick()
    await scrollToBottom()
  }
}

const executeTemplate = async (template: CommandTemplate) => {
  if (!selectedServer.value?.id) return

  commandHistory.value.push({
    command: template.command,
    output: '',
    isAi: false
  })

  await scrollToBottom()
  executing.value = true

  try {
    const result = await executeCommand({
      serverId: selectedServer.value.id,
      command: template.command,
      sessionId: sessionId.value
    })

    if (result.code === '0000') {
      const lastItem = commandHistory.value[commandHistory.value.length - 1]
      lastItem.output = result.data.output || ''
      lastItem.exitCode = result.data.exitCode
      lastItem.duration = result.data.duration
    }
  } catch (e: any) {
    const lastItem = commandHistory.value[commandHistory.value.length - 1]
    lastItem.output = e.message || 'Execution failed'
    lastItem.exitCode = -1
  } finally {
    executing.value = false
    await nextTick()
    await scrollToBottom()
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (terminalContainer.value) {
    terminalContainer.value.scrollTop = terminalContainer.value.scrollHeight
  }
}

const loadHistory = async () => {
  try {
    const result = await commandHistory(historyParams)
    if (result.code === '0000') {
      historyList.value = result.data.list || []
      historyTotal.value = result.data.total || 0
    }
  } catch {
    ElMessage.error('Failed to load history')
  }
}

const showOutput = (row: CommandHistory) => {
  currentOutput.value = row.output || 'No output'
  outputDialogVisible.value = true
}

const getStatusType = (status?: number) => {
  switch (status) {
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

const getStatusLabel = (status?: number) => {
  switch (status) {
    case 1: return 'Connected'
    case 2: return 'Error'
    default: return 'Disconnected'
  }
}

const loadTemplates = async () => {
  try {
    const result = await aiGetTemplates()
    if (result.code === '0000') {
      commandTemplates.value = result.data || []
    }
  } catch {
    // ignore
  }
}

onMounted(() => {
  loadServers()
  loadTemplates()
})

onUnmounted(() => {
  // Clean up WebSocket
  if (ws.value) {
    ws.value.close()
  }
  // Clean up terminal
  if (terminal.value) {
    terminal.value.dispose()
  }
})
</script>

<style scoped>
.server-management {
  padding: 20px;
  height: calc(100vh - 140px);
}

.server-list-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 15px;
}

.server-list {
  max-height: calc(100% - 150px);
  overflow-y: auto;
}

.server-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.server-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.server-item.active {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.server-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.server-host {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.server-meta {
  display: flex;
  gap: 8px;
}

.server-pagination {
  margin-top: 15px;
}

.terminal-card {
  height: 100%;
}

.terminal-actions {
  display: flex;
  gap: 10px;
}

.ai-command-section {
  margin-bottom: 15px;
}

.ai-input {
  margin-bottom: 10px;
}

.command-templates {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.template-label {
  font-size: 12px;
  color: #909399;
}

.template-btn {
  margin-right: 0;
}

.terminal-container {
  height: calc(100% - 180px);
  min-height: 300px;
  background-color: #1e1e1e;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 10px;
}

.xterm-container {
  height: 100%;
  padding: 10px;
}

.terminal-output {
  padding: 15px;
}

.terminal-item {
  margin-bottom: 15px;
}

.terminal-command {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 5px;
}

.prompt {
  color: #4caf50;
  font-weight: bold;
  font-family: monospace;
}

.command-text {
  color: #ffffff;
  font-family: monospace;
}

.ai-badge {
  background-color: #ff9800;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 3px;
}

.terminal-result {
  background-color: #2d2d2d;
  color: #d4d4d4;
  padding: 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
}

.terminal-result.error {
  color: #f44336;
}

.terminal-exit {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.terminal-input-container {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background-color: #1e1e1e;
  border-radius: 4px;
}

.terminal-input {
  flex: 1;
  background-color: transparent;
  border: none;
  color: #ffffff;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  outline: none;
}

.output-pre {
  background-color: #1e1e1e;
  color: #d4d4d4;
  padding: 20px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 500px;
  overflow-y: auto;
}
</style>
