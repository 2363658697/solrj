package solr;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.GroupParams;
import org.junit.Test;
/**
 * managed-schema文件定义的自段：其中foodName使用ik分词
<field name="foodId" type="int" indexed="false" stored="true"/>
<field name="foodName" type="text_ik" indexed="true" stored="true"/>
<field name="price" type="int" indexed="false" stored="true"/>
<field name="typeName" type="string" indexed="true" stored="true"/>

测试数据：
       id     foodName    price   foodType
       1        青椒炒蛋        10      湘菜
       2       麻辣豆腐         12      川菜
       3       虾仁炒蛋         25     粤菜
       4       臭豆腐            15      湘菜
 */
public class TestSolrJ {

    // 请求的url
    static String urlString = "http://localhost:8080/solr/core2";

    static SolrClient solr;
    static {
        solr = new HttpSolrClient(urlString);
    }

    @Test
    public void write() throws SolrServerException, IOException {
        // 创建document
        SolrInputDocument document = new SolrInputDocument();
        // 添加field
        document.addField("id", "4");
        document.addField("foodId", "4");
        document.addField("foodName", "虾仁炒蛋");
        document.addField("price", "25");
        document.addField("typeName", "粤菜");

        // 添加document到索引库
        solr.add(document);
        solr.commit();
        solr.close();
    }

    @Test
    public void read() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        // 指定查询条件,该方法和SolrQuery的构造器传入参数效果等同
        solrQuery.setQuery("*:*");
        // 过滤：对查询出来的结果进行筛选--指定查询的条件，与得分无关
        solrQuery.setFilterQueries("foodName:炒蛋");
        // 排序
        solrQuery.setSort("id", ORDER.desc);
        // 分页:开始索引，索引从0开始
        solrQuery.setStart(0);
        // 指定返回的document个数--总行数
        solrQuery.setRows(2);
        // 对查询的条件进行绑定
        QueryResponse query = solr.query(solrQuery);
        // 返回查询的数据(document)
        SolrDocumentList results = query.getResults();
        // 遍历数据
        for (SolrDocument solrDocument : results) {
            System.out.println(solrDocument.getFieldValue("id"));
            System.out.println(solrDocument.getFieldValue("foodName"));
            System.out.println(solrDocument.getFieldValue("price"));
            System.out.println(solrDocument.getFieldValue("typeName"));
        }
        solr.close();
    }

    @Test // 高亮不能使用过滤FilterQueries
    public void readHighlighting() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        // 指定查询条件,该方法和SolrQuery的构造器传入参数效果等同
        solrQuery.setQuery("foodName:炒蛋");
        // 排序
        solrQuery.setSort("id", ORDER.desc);
        // 分页:开始索引，索引从0开始
        solrQuery.setStart(0);
        // 指定返回的document个数--总行数
        solrQuery.setRows(2);
        // 开启高亮
        solrQuery.setHighlight(true);
        // 指定高亮的条件
        solrQuery.addHighlightField("foodName");
        // 效果等同---solrQuery.set("hl.fl", "foodName");
        // --solrQuery.set(HighlightParams.FIELDS, "foodName");
        // 设置高亮的样式
        solrQuery.setHighlightSimplePre("<font color=red>");
        solrQuery.setHighlightSimplePost("</font>");
        // 对查询的条件进行绑定
        QueryResponse query = solr.query(solrQuery);
        // 返回查询的数据(document)
        SolrDocumentList results = query.getResults();
        // 返回高亮数据
        Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
        // 遍历数据
        for (SolrDocument solrDocument : results) {
            String id = solrDocument.getFieldValue("id").toString();
            // 根据id获取高亮的数据集合
            Map<String, List<String>> map = highlighting.get(id);
            List<String> list = map.get("foodName");
            String hlString = list.get(0);
            System.out.println(hlString);
        }
        solr.close();
    }

    @Test
    public void deleteById() throws SolrServerException, IOException {
        // 根据id来删除数据
        solr.deleteById("1");
        solr.commit();
        solr.close();
    }

    @Test
    public void deleteByCondition() throws SolrServerException, IOException {
        // 根据条件来删除数据
        solr.deleteByQuery("foodName:青椒");
        solr.commit();
        solr.close();
    }

    @Test
    public void facet() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery("foodName:炒蛋");
        solrQuery.setFacet(true); // 开启分类
        solrQuery.setRows(0);
        solrQuery.addFacetField("typeName"); // 按字段分类 相同的归于一类
        solrQuery.setFacetLimit(10);// 限制facet返回的数量
        solrQuery.setFacetMissing(false);// 不统计null的值
        solrQuery.setFacetMinCount(1);// 设置返回的数据中每个分组的数据最小值，比如设置为1，则统计数量最小为1，不然不显示
        // solrQuery.setFacetSort("count asc"); //根据 count 数量 升序和降序 也可以根据索引
        // 默认是降序
        QueryResponse query = solr.query(solrQuery); // 对查询的条件进行绑定
        // System.out.println("查询时间：" + query.getQTime());
        List<FacetField> facets = query.getFacetFields();// 返回的facet列表
        for (FacetField facet : facets) {
            System.out.println(facet.getName());// 获取分组的条件--typeName
            List<Count> counts = facet.getValues();
            for (Count count : counts) {
                // 打印分组的结果和数量
                System.out.println(count.getName() + ":" + count.getCount());
            }
        }
        solr.close();
    }

    @Test
    public void GroupFieldQuery() throws Exception {
        SolrQuery solrQuery = new SolrQuery("foodName:炒蛋");
        // 开启group功能
        solrQuery.setParam(GroupParams.GROUP, true);
        // 指定group条件
        solrQuery.setParam(GroupParams.GROUP_FIELD, "typeName");
        // solrQuery.setParam("group.ngroups", true); --开启分组统计
        // 设置获取的数据数量,默认只获取分组中的第一条
        solrQuery.setParam(GroupParams.GROUP_LIMIT, "5");
        // 分页
        solrQuery.setStart(0);
        solrQuery.setRows(5);
        QueryResponse query = solr.query(solrQuery);
        GroupResponse groupResponse = query.getGroupResponse();
        List<GroupCommand> groupList = groupResponse.getValues();
        for (GroupCommand groupCommand : groupList) {
            System.out.println(groupCommand.getName()); // 获取group的条件
            List<Group> groups = groupCommand.getValues();
            for (Group group : groups) {
                SolrDocumentList results = group.getResult();
                // System.out.println(group.getGroupValue()+"-------"+results.size());
                // 获取group的结果和数量
                System.out.println(group.getGroupValue() + "====" + group.getResult().getNumFound());
                for (SolrDocument doc : results) {
                    // 获取group后的数据内容，可以获取到document中的所有属性(field)
                    System.out.println(doc.getFieldValue("foodName") + ":" + doc.getFieldValue("typeName") + ":" + doc.getFieldValue("price"));
                }
            }
        }
        solr.close();
    }

}
