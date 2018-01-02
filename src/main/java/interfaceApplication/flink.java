package interfaceApplication;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Model.CommonModel;
import common.java.JGrapeSystem.rMsg;
import common.java.apps.appsProxy;
import common.java.authority.plvDef.plvType;
import common.java.check.checkHelper;
import common.java.database.dbFilter;
import common.java.interfaceModel.GrapeDBSpecField;
import common.java.interfaceModel.GrapeTreeDBModel;
import common.java.nlogger.nlogger;
import common.java.security.codec;
import common.java.string.StringHelper;

public class flink {
    private GrapeTreeDBModel flink;
    private GrapeDBSpecField gDbSpecField;
    private CommonModel model;

    public flink() {
        model = new CommonModel();

        flink = new GrapeTreeDBModel();
        gDbSpecField = new GrapeDBSpecField();
        gDbSpecField.importDescription(appsProxy.tableConfig("flink"));
        flink.descriptionModel(gDbSpecField);
        flink.bindApp();
    }

    /**
     * 参数验证
     * 
     * @param param
     * @return
     */
    @SuppressWarnings("unchecked")
    private String checkParam(String param) {
        String url = "";
        String result = rMsg.netMSG(1, "参数不合法");
        JSONObject object = JSONObject.toJSON(param);
        if (object != null && object.size() > 0) {
            if (object.containsKey("email")) {
                String email = object.get("email").toString();
                if (("").equals(email) || !checkHelper.checkEmail(email)) {
                    result = rMsg.netMSG(2, "email格式错误");
                }
            }
            if (object.containsKey("url")) {
                url = object.getString("url");
                object.put("url", url.equals("") ? "" : codec.DecodeHtmlTag(url));
                result = object.toJSONString();
            }
        }
        return result;
    }

    /**
     * 新增友情链接
     * 
     * @param info
     * @return
     */
    @SuppressWarnings("unchecked")
    public String flinkAdd(String info) {
        Object temp;
        info = checkParam(info);
        if (!StringHelper.InvaildString(info)) {
            return rMsg.netMSG(1, "非法参数");
        }
        JSONObject obj = null;
        String result = rMsg.netMSG(100, "友情链接新增失败");
        if (info.contains("errorcode")) {
            return info;
        }
        JSONObject object = JSONObject.toJSON(info);
        JSONObject rMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 100);// 设置默认查询权限
        JSONObject uMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 200);
        JSONObject dMode = new JSONObject(plvType.chkType, plvType.powerVal).puts(plvType.chkVal, 300);
        object.put("rMode", rMode.toJSONString()); // 添加默认查看权限
        object.put("uMode", uMode.toJSONString()); // 添加默认修改权限
        object.put("dMode", dMode.toJSONString()); // 添加默认删除权限
        if (object != null && object.size() > 0) {
            temp = flink.data(object).insertOnce();
            obj = (temp != null) ? find(temp.toString()) : new JSONObject();
            result = rMsg.netMSG(0, obj);
        }
        return result;
    }

    /**
     * 修改友链
     * 
     * @param mid
     * @param msgInfo
     * @return
     */
    public String UpdateFlink(String mid, String msgInfo) {
        boolean objects = false;
        String result = rMsg.netMSG(100, "友情链接修改失败");
        if (StringHelper.InvaildString(msgInfo) && StringHelper.InvaildString(mid) && ObjectId.isValid(mid)) {
            JSONObject object = JSONObject.toJSON(msgInfo);
            if (object != null && object.size() > 0) {
                objects = flink.eq("_id", mid).data(object).updateEx();
            }
        }
        result = objects ? rMsg.netMSG(0, "友情链接修改成功") : result;
        return result;
    }

    /**
     * 删除友链
     * 
     * @param mid
     * @return
     */
    public String DeleteFlink(String mid) {
        return DeleteBatchFlink(mid);
    }

    /**
     * 批量删除友链
     * 
     * @param mids
     * @return
     */
    public String DeleteBatchFlink(String mids) {
        long code = 0;
        dbFilter filter = new dbFilter();
        String[] value = null;
        String result = rMsg.netMSG(100, "友情链接删除失败");
        if (StringHelper.InvaildString(mids)) {
            value = mids.split(",");
        }
        if (value != null) {
            for (String id : value) {
                if (StringHelper.InvaildString(id)) {
                    if (ObjectId.isValid(id) || checkHelper.isInt(id)) {
                        filter.eq("_id", id);
                    }
                }
            }
            JSONArray condArray = filter.build();
            if (condArray != null && condArray.size() > 0) {
                code = flink.or().where(condArray).deleteAll();
                result = code >= 0 ? rMsg.netMSG(0, "友情链接删除成功") : result;
            }
        }
        return result;
    }

    /**
     * 搜索友链
     * 
     * @param msgInfo
     * @return
     */
    public String SearchFlink(String msgInfo) {
        JSONArray array = null;
        JSONArray condArray = model.buildCond(msgInfo);
        if (condArray != null && condArray.size() > 0) {
            array = flink.where(condArray).select();
        }
        return rMsg.netMSG(true, (array != null && array.size() > 0) ? array : new JSONArray());
    }

    /**
     * 分页
     * 
     * @param idx
     * @param pageSize
     * @return
     */
    public String PageFlink(int idx, int pageSize) {
        return PageByFlink(idx, pageSize, null);
    }

    /**
     * 条件分页
     * 
     * @param idx
     * @param pageSize
     * @param msgInfo
     * @return
     */
    public String PageByFlink(int idx, int pageSize, String msgInfo) {
        long total = 0;
        JSONArray array = null;
        if (StringHelper.InvaildString(msgInfo)) {
            JSONArray condArray = model.buildCond(msgInfo);
            if (condArray != null && condArray.size() > 0) {
                flink.where(condArray);
            } else {
                return rMsg.netMSG(1, "无效参数");
            }
        }
        array = flink.dirty().page(idx, pageSize);
        total = flink.count();
        return rMsg.netPAGE(idx, pageSize, total, (array != null && array.size() > 0) ? array : new JSONArray());
    }

    /**
     * 查询所属某个网站的flink
     * 
     * @param wbid
     * @return
     */
    public String findLink(String wbid) {
        JSONArray array = flink.eq("wbid", wbid).select();
        return rMsg.netMSG(true, (array != null && array.size() > 0) ? array : new JSONArray());

    }

    /**
     * 查询
     * 
     * @param linkid
     * @return
     */
    private JSONObject find(String linkid) {
        JSONObject object = null;
        try {
            object = flink.eq("_id", linkid).find();
        } catch (Exception e) {
            nlogger.logout(e);
            object = null;
        }
        return (object != null && object.size() > 0) ? object : new JSONObject();
    }
}
