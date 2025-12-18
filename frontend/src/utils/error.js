export function extractErrorMessage(error, fallback = '') {
  const backendMessage = error?.response?.data?.message;
  if (typeof backendMessage === 'string' && backendMessage.trim()) {
    return backendMessage.trim();
  }
  const message = error?.message;
  if (typeof message === 'string' && message.trim()) {
    return message.trim();
  }
  return fallback;
}

export function buildDisplayError(prefix, error, fallback) {
  const detail = extractErrorMessage(error, fallback);
  if (detail) {
    return prefix ? `${prefix}：${detail}` : detail;
  }
  return prefix;
}
