package gdufs.yixiu.util;

import gdufs.yixiu.vo.NotifyPushVO;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LongPullingNotifier {
    // userId -> waiting results
    private static final Map<Integer, List<DeferredResult<NotifyPushVO>>> WAITING =
            new ConcurrentHashMap<>();

    public static void add(Integer userId, DeferredResult<NotifyPushVO> result) {
        WAITING.computeIfAbsent(userId, k -> new ArrayList<>()).add(result);
    }

    public static void notifyUser(Integer userId, NotifyPushVO vo) {
        List<DeferredResult<NotifyPushVO>> list = WAITING.get(userId);
        if (list != null) {
            list.forEach(r -> r.setResult(vo));
            list.clear();
        }
    }

    public static void broadcast(NotifyPushVO vo) {
        WAITING.values().forEach(list -> {
            list.forEach(r -> r.setResult(vo));
            list.clear();
        });
    }

    public static void remove(Integer userId, DeferredResult<NotifyPushVO> result) {
        WAITING.computeIfPresent(userId, (k, list) -> {
            list.remove(result);
            return list.isEmpty() ? null : list;
        });
    }
}
