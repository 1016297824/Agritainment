<template>
  <view class="page">
    <view class="plot-header">
      <text class="plot-number">{{ plot.plotNumber }}</text>
      <text class="plot-name">{{ plot.name }}</text>
      <view class="plot-badges">
        <text class="badge" :class="plot.status">{{
          plot.status === "rented" ? "已租用" : "可租用"
        }}</text>
        <text class="badge area">{{ plot.area }}㎡</text>
      </view>
    </view>

    <view v-if="plot.description" class="section">
      <text class="section-title">地块信息</text>
      <text class="plot-desc">{{ plot.description }}</text>
    </view>

    <view v-if="cameras.length > 0" class="section">
      <text class="section-title">实时监控</text>
      <view class="camera-list">
        <view
          v-for="cam in cameras"
          :key="cam.id"
          class="camera-card"
          @tap="viewCamera(cam)"
        >
          <text class="camera-name">{{ cam.name }}</text>
          <text class="camera-status" :class="cam.status">{{
            cam.status === "online" ? "在线" : "离线"
          }}</text>
        </view>
      </view>
    </view>

    <view v-if="plot.status === 'available' && !alreadyRented" class="section">
      <view class="rent-btn" @tap="handleRent">
        <text class="rent-text">租用此地块</text>
      </view>
    </view>

    <view v-if="plot.status === 'available' && alreadyRented" class="section">
      <text class="empty-text">每位客户限租一块地</text>
    </view>

    <view v-if="plot.status === 'rented'" class="section">
      <text class="section-title">种植管理服务</text>
      <view v-for="s in gardenServices" :key="s.id" class="service-card">
        <view class="service-info">
          <text class="service-name">{{ s.name }}</text>
          <text class="service-price">¥{{ s.price }}</text>
        </view>
        <view class="service-btn" @tap="orderService(s)">
          <text class="service-btn-text">下单</text>
        </view>
      </view>
    </view>

    <view v-if="canBind" class="section">
      <view class="bind-btn" @tap="showBindModal = true">
        <text class="bind-btn-text">绑定客户地块</text>
      </view>
    </view>

    <view v-if="showBindModal" class="modal">
      <view class="modal-content">
        <text class="modal-title">绑定客户地块</text>
        <text class="modal-desc">输入客户12位身份码完成地块绑定</text>
        <input
          v-model="identityCode"
          class="modal-input"
          placeholder="输入客户身份码"
          maxlength="12"
        />
        <view class="modal-actions">
          <button @tap="showBindModal = false">取消</button>
          <button class="btn-confirm" @tap="handleBind">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { plantingApi } from "@/api";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const plot = ref({});
const cameras = ref([]);
const gardenServices = ref([]);
const plotId = ref(0);
const showBindModal = ref(false);
const identityCode = ref("");

const canBind = computed(() => auth.isStaff || auth.isAdmin);
const alreadyRented = ref(false);

onMounted(async () => {
  const pages = getCurrentPages();
  const page = pages[pages.length - 1];
  plotId.value = Number(page.options?.id || 0);
  if (plotId.value) {
    await loadPlot();
    await loadServices();
    await checkAlreadyRented();
  }
});

const loadPlot = async () => {
  try {
    plot.value = await plantingApi.getPlot(plotId.value);
    if (plot.value.cameras) cameras.value = plot.value.cameras;
  } catch (e) {}
};

const loadServices = async () => {
  try {
    gardenServices.value = await plantingApi.getGardenServices();
  } catch (e) {}
};

const checkAlreadyRented = async () => {
  if (auth.isStaff || auth.isAdmin) {
    alreadyRented.value = false;
    return;
  }
  try {
    const plots = await plantingApi.getPlots();
    alreadyRented.value = plots.some(
      p => p.renterId == auth.userId && p.status === "rented"
    );
  } catch (e) {}
};

const handleRent = async () => {
  try {
    await plantingApi.rentPlot(plotId.value);
    uni.showToast({ title: "租用成功", icon: "success" });
    loadPlot();
  } catch (e) {}
};

const handleBind = async () => {
  if (!identityCode.value) {
    uni.showToast({ title: "请输入客户身份码", icon: "none" });
    return;
  }
  try {
    await plantingApi.bindPlotToUser(plotId.value, identityCode.value);
    uni.showToast({ title: "绑定成功", icon: "success" });
    showBindModal.value = false;
    identityCode.value = "";
    loadPlot();
  } catch (e) {
    uni.showToast({ title: e.message || "绑定失败", icon: "none" });
  }
};

const viewCamera = cam => {
  uni.showToast({ title: "摄像头功能开发中", icon: "none" });
};

const orderService = service => {
  uni.navigateTo({
    url: `/pages/planting/service-order?plotId=${plotId.value}&serviceId=${service.id}`
  });
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-background);
}
.plot-header {
  padding: 32rpx;
  background: var(--color-surface);
}
.plot-number {
  font-size: 36rpx;
  font-weight: 700;
  color: var(--color-text);
  display: block;
}
.plot-name {
  font-size: 28rpx;
  color: var(--color-muted);
  display: block;
  margin-top: 4rpx;
}
.plot-badges {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
}
.badge {
  font-size: 22rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
}
.badge.rented {
  background: #d1fae5;
  color: var(--color-primary);
}
.badge.available {
  background: #dbeafe;
  color: #1d4ed8;
}
.badge.area {
  background: #f3f4f6;
  color: var(--color-muted);
}
.section {
  padding: 32rpx;
}
.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 24rpx;
  display: block;
}
.plot-desc {
  font-size: 26rpx;
  color: var(--color-muted);
  line-height: 1.6;
}
.camera-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.camera-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
.camera-name {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
}
.camera-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.camera-status.online {
  background: #d1fae5;
  color: var(--color-primary);
}
.camera-status.offline {
  background: #fee2e2;
  color: var(--color-error);
}
.rent-btn {
  background: var(--color-primary);
  border-radius: var(--radius-sm);
  padding: 28rpx;
  text-align: center;
}
.rent-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
}
.service-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}
.service-info {
  flex: 1;
}
.service-name {
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-text);
  display: block;
}
.service-price {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--color-cta);
  display: block;
  margin-top: 4rpx;
}
.service-btn {
  background: var(--color-cta);
  padding: 12rpx 32rpx;
  border-radius: var(--radius-sm);
}
.service-btn-text {
  color: #fff;
  font-size: 26rpx;
  font-weight: 500;
}
.bind-btn {
  background: var(--color-cta);
  border-radius: var(--radius-sm);
  padding: 28rpx;
  text-align: center;
}
.bind-btn-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
}
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}
.modal-content {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  width: 80%;
}
.modal-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8rpx;
  display: block;
}
.modal-desc {
  font-size: 26rpx;
  color: #6b7280;
  margin-bottom: 16rpx;
  display: block;
}
.modal-input {
  border: 2rpx solid #d1d5db;
  border-radius: 8rpx;
  padding: 12rpx 16rpx;
  margin-bottom: 12rpx;
  font-size: 28rpx;
}
.modal-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 16rpx;
}
.btn-confirm {
  background: #15803d;
  color: #fff;
  border-radius: 8rpx;
}
.empty-text {
  color: var(--color-muted);
  font-size: 28rpx;
}
</style>
