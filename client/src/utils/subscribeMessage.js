const TEMPLATE_IDS = {
  reservationCreated: '',
  reservationCancelled: '',
  serviceOrderCreated: ''
}

export function requestSubscribeMessage(templateIds) {
  const ids = templateIds || Object.values(TEMPLATE_IDS).filter(id => id)
  if (!ids.length) return Promise.resolve()
  return new Promise((resolve) => {
    uni.requestSubscribeMessage({
      tmplIds: ids,
      success: () => resolve(),
      fail: () => resolve()
    })
  })
}

export function requestReservationNotify() {
  const ids = [TEMPLATE_IDS.reservationCreated, TEMPLATE_IDS.reservationCancelled].filter(id => id)
  return requestSubscribeMessage(ids)
}

export function requestServiceOrderNotify() {
  const ids = [TEMPLATE_IDS.serviceOrderCreated].filter(id => id)
  return requestSubscribeMessage(ids)
}
