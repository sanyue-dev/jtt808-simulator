package cn.org.hentai.simulator.web.controller;

import cn.org.hentai.simulator.domain.model.DrivePlan;
import cn.org.hentai.simulator.domain.model.Point;
import cn.org.hentai.simulator.service.RouteManager;
import cn.org.hentai.simulator.infrastructure.util.MD5;
import cn.org.hentai.simulator.domain.entity.Route;
import cn.org.hentai.simulator.domain.entity.RoutePoint;
import cn.org.hentai.simulator.domain.entity.StayPoint;
import cn.org.hentai.simulator.domain.entity.TroubleSegment;
import cn.org.hentai.simulator.service.RoutePointService;
import cn.org.hentai.simulator.service.RouteService;
import cn.org.hentai.simulator.service.StayPointService;
import cn.org.hentai.simulator.service.TroubleSegmentService;
import cn.org.hentai.simulator.web.vo.Page;
import cn.org.hentai.simulator.web.vo.Result;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by houcheng when 2018/11/25.
 * 线路控制器
 */
@Controller
@RequestMapping("/route")
public class RouteController
{
    @Autowired
    RouteService routeService;

    @Autowired
    private RoutePointService routePointService;

    @Autowired
    private StayPointService stayPointService;

    @Autowired
    private TroubleSegmentService troubleSegmentService;

    @Value("${map.baidu.key}")
    String baiduMapKey;

    @RequestMapping("/index")
    public String index()
    {
        return "routes";
    }

    @RequestMapping("/list")
    @ResponseBody
    public Result list(@RequestParam(defaultValue = "1") int pageIndex, @RequestParam(defaultValue = "20") int pageSize)
    {
        Result result = new Result();
        try
        {
            Page<Route> routes = routeService.find(null, pageIndex, pageSize);

            result.setData(routes);
        } catch (Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }

    /**
     * 创建线路并跳转到编辑页面
     *
     * @return
     */
    @RequestMapping("/create")
    public String create(Model model)
    {
        fillRoutePageModel(model, "create", null, null, null, null);
        return "route-create";
    }

    @RequestMapping("/edit")
    public String edit(@RequestParam Long id, Model model)
    {
        Route route = routeService.getById(id);
        if (route == null) throw new RuntimeException("no such route: " + id);

        List<RoutePoint> points = routePointService.find(id);
        List<StayPoint> stayPoints = stayPointService.find(id);
        List<TroubleSegment> troubleSegments = troubleSegmentService.find(id);
        if (points != null)
        {
            points.sort(Comparator.comparing(RoutePoint::getId));
            for (RoutePoint point : points)
            {
                point.setLng(point.getLongitude());
                point.setLat(point.getLatitude());
            }
        }
        fillRoutePageModel(model, "edit", route, points, stayPoints, troubleSegments);
        return "route-create";
    }

    @RequestMapping("/create/save")
    @ResponseBody
    public Result createSave(@RequestParam String name,
                             @RequestParam Integer minSpeed,
                             @RequestParam Integer maxSpeed,
                             @RequestParam Integer mileages,
                             @RequestParam String stationsJsonText,
                             @RequestParam String pointsJsonText,
                             @RequestParam String stayPointsJsonText,
                             @RequestParam String segmentsJsonText)
    {
        Result result = new Result();
        try
        {
            Route route = buildRoute(null, name, minSpeed, maxSpeed, mileages, stationsJsonText, pointsJsonText, stayPointsJsonText, segmentsJsonText);
            routeService.create(route);
            saveRouteDetails(route, pointsJsonText, stayPointsJsonText, segmentsJsonText);
        }
        catch (Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    @RequestMapping("/edit/save")
    @ResponseBody
    public Result editSave(@RequestParam Long id,
                           @RequestParam String name,
                           @RequestParam Integer minSpeed,
                           @RequestParam Integer maxSpeed,
                           @RequestParam Integer mileages,
                           @RequestParam String stationsJsonText,
                           @RequestParam String pointsJsonText,
                           @RequestParam String stayPointsJsonText,
                           @RequestParam String segmentsJsonText)
    {
        Result result = new Result();
        try
        {
            Route route = routeService.getById(id);
            if (route == null) throw new RuntimeException("no such route: " + id);

            route = buildRoute(route, name, minSpeed, maxSpeed, mileages, stationsJsonText, pointsJsonText, stayPointsJsonText, segmentsJsonText);
            routeService.update(route);
            saveRouteDetails(route, pointsJsonText, stayPointsJsonText, segmentsJsonText);
        }
        catch (Exception e)
        {
            result.setError(e);
        }
        return result;
    }

    @RequestMapping("/remove")
    @ResponseBody
    public Result remove(@RequestParam Long id)
    {
        Result result = new Result();
        try
        {
            routeService.removeById(id);
            routePointService.removeByRouteId(id);
            stayPointService.removeByRouteId(id);
            troubleSegmentService.removeByRouteId(id);
            RouteManager.getInstance().remove(id);
        }
        catch(Exception ex)
        {
            result.setError(ex);
        }
        return result;
    }

    private void fillRoutePageModel(Model model, String pageMode, Route route, List<RoutePoint> points, List<StayPoint> stayPoints, List<TroubleSegment> troubleSegments)
    {
        Gson gson = new Gson();
        model.addAttribute("baiduMapKey", baiduMapKey);
        model.addAttribute("pageMode", pageMode);
        model.addAttribute("routeJson", route == null ? "null" : gson.toJson(route));
        model.addAttribute("stationsJson", route == null || StringUtils.isEmpty(route.getStationsJson()) ? "[]" : route.getStationsJson());
        model.addAttribute("pointsJson", points == null ? "[]" : gson.toJson(points));
        model.addAttribute("stayPointsJson", stayPoints == null ? "[]" : gson.toJson(stayPoints));
        model.addAttribute("troubleSegmentsJson", troubleSegments == null ? "[]" : gson.toJson(troubleSegments));
    }

    private Route buildRoute(Route route,
                             String name,
                             Integer minSpeed,
                             Integer maxSpeed,
                             Integer mileages,
                             String stationsJsonText,
                             String pointsJsonText,
                             String stayPointsJsonText,
                             String segmentsJsonText)
    {
        Route target = route == null ? new Route() : route;
        target.setName(name);
        target.setMinSpeed(minSpeed);
        target.setMaxSpeed(maxSpeed);
        target.setMileages(mileages);
        target.setStationsJson(stationsJsonText);

        StringBuilder signature = new StringBuilder(4096 * 10);
        signature.append(String.valueOf(minSpeed));
        signature.append(String.valueOf(maxSpeed));
        signature.append(String.valueOf(mileages));
        signature.append(stationsJsonText);
        signature.append(pointsJsonText);
        signature.append(stayPointsJsonText);
        signature.append(segmentsJsonText);
        target.setFingerPrint(MD5.encode(signature.toString()));
        return target;
    }

    private void saveRouteDetails(Route route,
                                  String pointsJsonText,
                                  String stayPointsJsonText,
                                  String segmentsJsonText)
    {
        long id = route.getId();
        Gson gson = new Gson();

        List<RoutePoint> points = parseList(pointsJsonText, new TypeToken<List<RoutePoint>>() {}.getType(), gson);
        if (points != null)
        {
            for (RoutePoint point : points)
            {
                point.setId(null);
                point.setLatitude(point.getLat());
                point.setLongitude(point.getLng());
                point.setRouteId(id);
            }
            routePointService.batchSave(route, points);
        }

        List<StayPoint> stayPoints = parseList(stayPointsJsonText, new TypeToken<List<StayPoint>>() {}.getType(), gson);
        if (stayPoints != null)
        {
            for (StayPoint stayPoint : stayPoints)
            {
                stayPoint.setId(null);
                stayPoint.setRouteid(id);
            }
            stayPointService.save(route, stayPoints);
        }

        List<TroubleSegment> troubleSegments = parseList(segmentsJsonText, new TypeToken<List<TroubleSegment>>() {}.getType(), gson);
        if (troubleSegments != null)
        {
            for (TroubleSegment segment : troubleSegments)
            {
                segment.setId(null);
                segment.setRouteId(id);
            }
            troubleSegmentService.save(route, troubleSegments);
        }

        RouteManager.getInstance().load(route);
    }

    private <T> List<T> parseList(String jsonText, java.lang.reflect.Type type, Gson gson)
    {
        if (StringUtils.isEmpty(jsonText)) return null;
        return gson.fromJson(jsonText, type);
    }
}
