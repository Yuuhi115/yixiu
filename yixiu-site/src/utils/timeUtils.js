export function formatTime (time) {
    if (!time) return ''

    const date = new Date(time)
    const now = new Date()

    // 如果是今天，显示时间
    if (date.toDateString() === now.toDateString()) {
        return "今天 " + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }

    // 如果是今年，显示月日时间
    if (date.getFullYear() === now.getFullYear()) {
        return date.toLocaleDateString('zh-CN', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })
    }

    // 其他情况显示完整日期
    return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    })
}