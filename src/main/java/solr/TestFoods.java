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
        foods.setFoodName("Ϻ�ʳ���");
        foods.setPrice(10);
        foods.setTypeName("����");
        //��ӵ�������
        solr.addBean(foods);
        solr.commit();
        solr.close();
    }

    @Test
    public void read() throws SolrServerException, IOException {
        SolrQuery sq=new SolrQuery();  
        sq.setQuery("*:*");  
        //����
        sq.setFilterQueries("foodName:����");
        //����
        sq.setSort("foodName",ORDER.desc);
        //��ҳ
        sq.setStart(0);
        sq.setRows(2);
        QueryResponse query = solr.query(sq);
        //��ȡ��ѯ������
        List<Foods> beans = query.getBeans(Foods.class);
        for (Foods foods : beans) {
                System.out.println(foods.getFoodName()+":"+foods.getTypeName());
        }
    }
    @Test
    public void readHighlighting() throws SolrServerException, IOException {
        SolrQuery sq=new SolrQuery();  
        sq.setQuery("foodName:����");  
        //����
        sq.setSort("foodName",ORDER.desc);
        //��ҳ
        sq.setStart(0);
        sq.setRows(2);
        //����
        //�Ƿ����
        sq.setHighlight(true);
        //ָ������������
        sq.addHighlightField("foodName");
        sq.setHighlightSimplePre("<font color=red>");
        sq.setHighlightSimplePost("</font>");
      //�Բ�ѯ���������а�
        QueryResponse query = solr.query(sq);
        //��ȡ��ѯ������
        List<Foods> beans = query.getBeans(Foods.class);
        //��ȡ����������
        Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
        for (Foods foods : beans) {
            Map<String, List<String>> map = highlighting.get(foods.getId());
            //��ȡ������������ȡ����
            List<String> list = map.get("foodName");
            String hlString=list.get(0);
            System.out.println(hlString);
        }
    }
    
    @Test
    public void deleteById() throws SolrServerException, IOException {
        // ����id��ɾ������
        solr.deleteById("1");
        solr.commit();
        solr.close();
    }

    @Test
    public void deleteByCondition() throws SolrServerException, IOException {
        // ����������ɾ������
        solr.deleteByQuery("foodName:�ཷ");
        solr.commit();
        solr.close();
    }

    @Test
    public void facet() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery("foodName:����");
        solrQuery.setFacet(true); // ��������
        solrQuery.setRows(0);
        solrQuery.addFacetField("typeName"); // ���ֶη��� ��ͬ�Ĺ���һ��
        solrQuery.setFacetLimit(10);// ����facet���ص�����
        solrQuery.setFacetMissing(false);// ��ͳ��null��ֵ
        solrQuery.setFacetMinCount(1);// ���÷��ص�������ÿ�������������Сֵ����������Ϊ1����ͳ��������СΪ1����Ȼ����ʾ
        // solrQuery.setFacetSort("count asc"); //���� count ���� ����ͽ��� Ҳ���Ը�������
        // Ĭ���ǽ���
        QueryResponse query = solr.query(solrQuery); // �Բ�ѯ���������а�
        // System.out.println("��ѯʱ�䣺" + query.getQTime());
        List<FacetField> facets = query.getFacetFields();// ���ص�facet�б�
        for (FacetField facet : facets) {
            System.out.println(facet.getName());// ��ȡ���������--typeName
            List<Count> counts = facet.getValues();
            for (Count count : counts) {
                // ��ӡ����Ľ��������
                System.out.println(count.getName() + ":" + count.getCount());
            }
        }
        solr.close();
    }

    @Test
    public void GroupFieldQuery() throws Exception {
        SolrQuery solrQuery = new SolrQuery("foodName:����");
        // ����group����
        solrQuery.setParam(GroupParams.GROUP, true);
        // ָ��group����
        solrQuery.setParam(GroupParams.GROUP_FIELD, "typeName");
        // solrQuery.setParam("group.ngroups", true); --��������ͳ��
        // ���û�ȡ����������,Ĭ��ֻ��ȡ�����еĵ�һ��
        solrQuery.setParam(GroupParams.GROUP_LIMIT, "5");
        // ��ҳ
        solrQuery.setStart(0);
        solrQuery.setRows(5);
        QueryResponse query = solr.query(solrQuery);
        GroupResponse groupResponse = query.getGroupResponse();
        List<GroupCommand> groupList = groupResponse.getValues();
        for (GroupCommand groupCommand : groupList) {
            System.out.println(groupCommand.getName()); // ��ȡgroup������
            List<Group> groups = groupCommand.getValues();
            for (Group group : groups) {
                
                SolrDocumentList results = group.getResult();
                // System.out.println(group.getGroupValue()+"-------"+results.size());
                // ��ȡgroup�Ľ��������
                System.out.println(group.getGroupValue() + "====" + group.getResult().getNumFound());
                for (SolrDocument doc : results) {
                    // ��ȡgroup����������ݣ����Ի�ȡ��document�е���������(field)
                    System.out.println(doc.getFieldValue("foodName") + ":" + doc.getFieldValue("typeName") + ":" + doc.getFieldValue("price"));
                }
            }
        }
        solr.close();
    }
}
