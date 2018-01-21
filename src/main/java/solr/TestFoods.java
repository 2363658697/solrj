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
import org.apache.solr.common.params.GroupParams;
import org.junit.Test;

public class TestFoods {

    static String urlString = "http://localhost:8080/solr/core2";

    static SolrClient solr;
    static {
        solr = new HttpSolrClient(urlString);
    }

    @Test
    public void write() throws IOException, SolrServerException {
        Foods foods = new Foods();
        foods.setId("2");
        foods.setFoodId(1);
        foods.setFoodName("虾仁炒蛋");
        foods.setPrice(10);
        foods.setTypeName("粤菜");
        //添加到索引库
        solr.addBean(foods);
        solr.commit();
        solr.close();
    }

    @Test
    public void read() throws SolrServerException, IOException {
        SolrQuery sq=new SolrQuery();  
        sq.setQuery("*:*");  
        //过滤
        sq.setFilterQueries("foodName:炒蛋");
        //排序
        sq.setSort("foodName",ORDER.desc);
        //分页
        sq.setStart(0);
        sq.setRows(2);
        QueryResponse query = solr.query(sq);
        //获取查询的数据
        List<Foods> beans = query.getBeans(Foods.class);
        for (Foods foods : beans) {
                System.out.println(foods.getFoodName()+":"+foods.getTypeName());
        }
    }
    @Test
    public void readHighlighting() throws SolrServerException, IOException {
        SolrQuery sq=new SolrQuery();  
        sq.setQuery("foodName:炒蛋");  
        //排序
        sq.setSort("foodName",ORDER.desc);
        //分页
        sq.setStart(0);
        sq.setRows(2);
        //高亮
        //是否高亮
        sq.setHighlight(true);
        //指定高亮的条件
        sq.addHighlightField("foodName");
        sq.setHighlightSimplePre("<font color=red>");
        sq.setHighlightSimplePost("</font>");
      //对查询的条件进行绑定
        QueryResponse query = solr.query(sq);
        //获取查询的数据
        List<Foods> beans = query.getBeans(Foods.class);
        //获取高亮的数据
        Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
        for (Foods foods : beans) {
            Map<String, List<String>> map = highlighting.get(foods.getId());
            //获取高亮的条件获取数据
            List<String> list = map.get("foodName");
            String hlString=list.get(0);
            System.out.println(hlString);
        }
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
