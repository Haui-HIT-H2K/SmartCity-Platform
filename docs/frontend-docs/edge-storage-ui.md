---
sidebar_position: 2
title: Edge Storage Management UI
---

# Edge Storage Management UI

## Tổng Quan

Trang Edge Storage Management cung cấp giao diện quản lý các edge storage nodes trong hệ thống.

## Vị Trí

**Route:** `/nodes`  
**Component:** `frontend/pages/nodes.vue`  
**Layout:** `default`

## Navigation

Menu item "Edge Storage" được thêm vào sidebar navigation:

```vue
<NuxtLink to="/nodes">
  <HardDrive class="w-5 h-5" />
  <span>Edge Storage</span>
</NuxtLink>
```

**Position:** Sau System Control, trước Footer  
**Icon:** HardDrive (lucide-vue-next)  
**Active State:** Primary color với border khi active

## UI Components

### 1. Page Header

```vue
<div class="flex items-center justify-between">
  <div>
    <h1>Edge Node Management</h1>
    <p>Manage edge storage nodes</p>
  </div>
  <UiButton @click="showAddDialog = true">
    <Plus class="w-4 h-4" />
    <span>Add Node</span>
  </UiButton>
</div>
```

**Features:**
- Title và subtitle
- "Add Node" button ở góc phải

### 2. Data Table

Table hiển thị danh sách edge nodes với các cột:

| Column | Type | Description |
|--------|------|-------------|
| Name | Text | Display name của node |
| Host | Text (monospace) | Hostname của RabbitMQ |
| Port | Number | Port number |
| Status | Badge | Online (green) / Offline (gray) |
| Enabled | Toggle Switch | Enable/disable node |
| Actions | Icon Button | Delete button (trash icon) |

**Styling:**
- Glass card effect với border
- Hover effect trên rows
- Border between rows
- Responsive table layout

### 3. Loading State

```vue
<div v-if="loading">
  <div class="animate-spin rounded-full h-12 w-12 border-b-2" />
  <p>Loading nodes...</p>
</div>
```

**When Shown:**
- Initial page load
- After CRUD operations (briefly)

### 4. Empty State

```vue
<div v-if="nodes.length === 0">
  <Database class="w-16 h-16" />
  <h3>No Edge Nodes</h3>
  <p>Add your first edge node to get started</p>
</div>
```

**When Shown:**
- Khi chưa có node nào trong hệ thống

### 5. Add Node Dialog

Modal dialog để thêm edge node mới.

**Form Fields:**

```vue
<form @submit.prevent="addNode">
  <input v-model="newNode.name" required placeholder="Subnet-CauGiay" />
  <input v-model="newNode.host" required placeholder="rabbit-edge-1" />
  <input v-model.number="newNode.port" type="number" required placeholder="5672" />
  <input v-model="newNode.queueName" placeholder="city-data-queue-1 (optional)" />
  <input v-model="newNode.username" placeholder="edge_user (optional)" />
  <input v-model="newNode.password" type="password" placeholder="edge_pass (optional)" />
</form>
```

**Validation:**
- Name: Required, string
- Host: Required, string
- Port: Required, number (default: 5672)
- QueueName: Optional, string
- Username: Optional, string
- Password: Optional, string (masked)

**Actions:**
- "Add Node" button (primary)
- "Cancel" button (ghost variant)

**Behavior:**
- Form reset sau khi submit thành công
- Dialog đóng tự động
- List refresh để hiển thị node mới
- Error alert nếu tên đã tồn tại

### 6. Delete Confirmation Dialog

Modal dialog xác nhận trước khi xóa.

```vue
<div v-if="deleteConfirm">
  <h2>Delete Node</h2>
  <p>
    Are you sure you want to delete 
    <span class="font-semibold">{{ deleteConfirm }}</span>?
    This action cannot be undone.
  </p>
  <UiButton @click="deleteNode">Delete</UiButton>
  <UiButton @click="deleteConfirm = null">Cancel</UiButton>
</div>
```

**Features:**
- Hiển thị tên node sẽ bị xóa
- Warning message rõ ràng
- Red "Delete" button
- "Cancel" button để hủy

## State Management

Component sử dụng Vue Composition API với refs:

```typescript
const nodes = ref<any[]>([]);           // Danh sách nodes
const loading = ref(true);              // Loading state
const showAddDialog = ref(false);       // Add dialog visibility
const deleteConfirm = ref<string | null>(null);  // Node name to delete
const newNode = ref({                   // Form data
  name: '',
  host: '',
  port: 5672,
  queueName: '',
  username: '',
  password: '',
});
```

## API Integration

### Fetch Nodes

```typescript
const fetchNodes = async () => {
  loading.value = true;
  const data = await $fetch('/api/nodes');
  nodes.value = data;
  loading.value = false;
};
```

**Endpoint:** `GET /api/nodes`  
**When Called:** onMounted, after CRUD operations

### Add Node

```typescript
const addNode = async () => {
  await $fetch('/api/nodes', {
    method: 'POST',
    body: newNode.value,
  });
  // Reset form
  newNode.value = { name: '', host: '', port: 5672, queueName: '', username: '', password: '' };
  showAddDialog.value = false;
  await fetchNodes();
};
```

**Endpoint:** `POST /api/nodes`  
**Error Handling:** Alert với error message

### Toggle Node

```typescript
const toggleNode = async (name: string) => {
  await $fetch(`/api/nodes/${'${name}'}/toggle`, {
    method: 'PUT',
  });
  await fetchNodes();
};
```

**Endpoint:** `PUT /api/nodes/{name}/toggle`  
**UI Update:** Optimistic - switch changes immediately, then confirms with fetch

### Delete Node

```typescript
const deleteNode = async () => {
  if (!deleteConfirm.value) return;
  await $fetch(`/api/nodes/${'${deleteConfirm.value}'}`, {
    method: 'DELETE',
  });
  deleteConfirm.value = null;
  await fetchNodes();
};
```

**Endpoint:** `DELETE /api/nodes/{name}`  
**Confirmation:** Required via dialog

## Server API Routes

Nuxt server routes proxy requests đến backend:

### GET Route

**File:** `server/api/nodes/index.get.ts`

```typescript
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || "http://localhost:8080";
  const nodes = await $fetch(`${'${backendUrl}'}/api/nodes`);
  return nodes;
```

### POST Route

**File:** `server/api/nodes/index.post.ts`

```typescript
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || "http://localhost:8080";
  const body = await readBody(event);
  const response = await $fetch(`${'${backendUrl}'}/api/nodes`, {
    method: "POST",
    body,
  });
  return response;
});
```

### Toggle Route

**File:** `server/api/nodes/[name]/toggle.put.ts`

```typescript
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || "http://localhost:8080";
  const name = getRouterParam(event, "name");
  const response = await $fetch(`${'${backendUrl}'}/api/nodes/${'${name}'}/toggle`, {
    method: "PUT",
  });
  return response;
});
```

### Delete Route

**File:** `server/api/nodes/[name]/index.delete.ts`

```typescript
export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig();
  const backendUrl = config.public.apiBase || "http://localhost:8080";
  const name = getRouterParam(event, "name");
  const response = await $fetch(`${'${backendUrl}'}/api/nodes/${'${name}'}`, {
    method: "DELETE",
  });
  return response;
});
```

**Purpose:** 
- Centralized backend URL configuration
- CORS handling
- Error transformation

## Styling

### Design System

- **Glass Card:** `glass-card` class với backdrop blur
- **Primary Color:** Blue gradient
- **Text Colors:**
  - Headers: `text-gray-100`
  - Body: `text-gray-400`
  - Accents: `text-primary`
- **Borders:** `border-dark-border`
- **Backgrounds:** `bg-dark-lighter`

### Status Colors

```css
Online: bg-green-500/20 text-green-400 border-green-500/40
Offline: bg-gray-500/20 text-gray-400 border-gray-500/40
```

### Toggle Switch

```vue
<button class="inline-flex h-6 w-11 rounded-full"
        :class="enabled ? 'bg-primary' : 'bg-gray-600'">
  <span class="h-4 w-4 rounded-full bg-white"
        :class="enabled ? 'translate-x-6' : 'translate-x-1'">
  </span>
</button>
```

**States:**
- ON: Primary color, dot moved right
- OFF: Gray color, dot moved left
- Transition: Smooth animation

## Responsive Design

- Desktop (≥1024px): Full table layout
- Tablet (768px-1023px): Scrollable table
- Mobile (&lt;768px): Should implement card layout (future enhancement)

---

## User Flow

### View Nodes

1. Click "Edge Storage" trong sidebar
2. Page loads, shows loading state
3. Danh sách nodes hiển thị trong table
4. User có thể scroll nếu nhiều nodes

### Add Node

1. Click "Add Node" button
2. Dialog mở với empty form
3. User nhập thông tin
4. Click "Add Node"
5. Validation check
6. API call
7. Success: Dialog đóng, list refresh
8. Error: Alert hiển thị

### Toggle Node

1. User click toggle switch
2. Switch animation plays
3. API call
4. Success: Status badge updates
5. Error: Switch reverts

### Delete Node

1. Click trash icon
2. Confirmation dialog hiển thị
3. User confirms  
4. API call
5. Success: Node removed from list
6. Error: Dialog đóng, error shown

---

## Error Handling

### Network Errors

```typescript
try {
  await $fetch('/api/nodes');
} catch (error) {
  console.error('Error fetching nodes:', error);
  // Show error state in UI
}
```

### Validation Errors

```typescript
catch (error: any) {
  alert(error.data?.error || 'Failed to add node');
}
```

### Best Practices

- Log errors to console
- Show user-friendly messages
- Don't expose technical details
- Provide actionable guidance

---

## Accessibility

### Keyboard Navigation

- Tab through interactive elements
- Enter to submit forms
- Escape to close dialogs

### ARIA Labels

```vue
<button aria-label="Delete node">
  <Trash2 />
</button>
```

### Screen Readers

- Semantic HTML elements
- Proper heading hierarchy
- Alt text cho icons

---

## Performance

### Optimization Techniques

- Lazy load icons với dynamic imports
- Debounce search/filter (future)
- Virtual scrolling for large lists (future)
- Optimize API calls (no unnecessary fetches)

### Loading States

- Skeleton screens (future enhancement)
- Spinner during data fetch
- Disabled buttons during operations

---

## Testing

### Component Tests

- [❌] Mount component with mock data
- [❌] Test form validation
- [❌] Test CRUD operations
- [❌] Test error handling

### E2E Tests

- [✅] Manual testing completed
- [❌] Automated Playwright tests

---

## Future Enhancements

- [ ] Search/filter functionality
- [ ] Sort table columns
- [ ] Bulk operations
- [ ] Export node list
- [ ] Import nodes từ CSV/JSON
- [ ] Real-time status updates
- [ ] Performance metrics per node
