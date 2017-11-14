package interfaceApplication;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import JGrapeSystem.rMsg;
import Model.CommonModel;
import apps.appsProxy;
import check.checkHelper;
import interfaceModel.GrapeDBSpecField;
import interfaceModel.GrapeTreeDBModel;
import security.codec;
import string.StringHelper;

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
	public String flinkAdd(String info) {
		Object temp;
		info = checkParam(info);
		JSONObject obj = null;
		String result = rMsg.netMSG(100, "友情链接新增失败");
		if (info.contains("errorcode")) {
			return info;
		}
		JSONObject object = JSONObject.toJSON(info);
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
		int code = 99;
		String result = rMsg.netMSG(100, "友情链接修改失败");
		if (!StringHelper.InvaildString(msgInfo) && !StringHelper.InvaildString(mid) && ObjectId.isValid(mid)) {
			JSONObject object = JSONObject.toJSON(msgInfo);
			if (object != null && object.size() > 0) {
				code = flink.eq("_id", mid).data(object).update() != null ? 0 : 99;
			}
		}
		result = code == 0 ? rMsg.netMSG(0, "友情链接修改成功") : result;
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
		String[] value = null;
		String result = rMsg.netMSG(100, "友情链接删除失败");
		if (!StringHelper.InvaildString(mids)) {
			value = mids.split(",");
		}
		if (value != null) {
			flink.or();
			for (String id : value) {
				if (ObjectId.isValid(id)) {
					flink.eq("_id", id);
				}
			}
			code = flink.deleteAll();
			result = code >= 0 ? rMsg.netMSG(0, "友情链接删除成功") : result;
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
		if (!StringHelper.InvaildString(msgInfo)) {
			JSONArray condArray = model.buildCond(msgInfo);
			if (condArray != null && condArray.size() > 0) {
				flink.where(condArray);
			} else {
				return rMsg.netPAGE(idx, pageSize, total, new JSONArray());
			}
		}
		array = flink.dirty().page(idx, pageSize);
		total = flink.count();
		return rMsg.netPAGE(idx, pageSize, total, (array != null && array.size() > 0) ? array : new JSONArray());
	}

	/**
	 * 查询所属某个网站的flink
	 * @param wbid
	 * @return
	 */
	public String findLink(String wbid) {
		JSONArray array = flink.eq("wbid", wbid).select();
		return rMsg.netMSG(true, (array != null && array.size() > 0) ? array : new JSONArray());

	}

	/**
	 * 查询
	 * @param linkid
	 * @return
	 */
	private JSONObject find(String linkid) {
		JSONObject object = null;
		object = flink.eq("_id", linkid).find();
		return (object != null && object.size() > 0) ? object : new JSONObject();
	}
}
