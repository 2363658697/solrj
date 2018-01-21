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
 * managed-schema�ļ�������ԶΣ�����foodNameʹ��ik�ִ�
<field name="foodId" type="int" indexed="false" stored="true"/>
<field name="foodName" type="text_ik" indexed="true" stored="true"/>
<field name="price" type="int" indexed="false" stored="true"/>
<field name="typeName" type="string" indexed="true" stored="true"/>

�������ݣ�
       id     foodName    price   foodType
       1        �ཷ����        10      ���
       2       ��������         12      ����
       3       Ϻ�ʳ���         25     ����
       4       ������            15      ���
 */
public class TestSolrJ {

    // �����url
    static String urlString = "http://localhost:8080/solr/core2";

    static SolrClient solr;
    static {
        solr = new HttpSolrClient(urlString);
    }

    @Test
    public void write() throws SolrServerException, IOException {
        // ����document
        SolrInputDocument document = new SolrInputDocument();
        // ���field
        document.addField("id", "4");
        document.addField("foodId", "4");
        document.addField("foodName", "Ϻ�ʳ���");
        document.addField("price", "25");
        document.addField("typeName", "����");

        // ���document��������
        solr.add(document);
        solr.commit();
        solr.close();
    }

    @Test
    public void read() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        // ָ����ѯ����,�÷�����SolrQuery�Ĺ������������Ч����ͬ
        solrQuery.setQuery("*:*");
        // ���ˣ��Բ�ѯ�����Ľ������ɸѡ--ָ����ѯ����������÷��޹�
        solrQuery.setFilterQueries("foodName:����");
        // ����
        solrQuery.setSort("id", ORDER.desc);
        // ��ҳ:��ʼ������������0��ʼ
        solrQuery.setStart(0);
        // ָ�����ص�document����--������
        solrQuery.setRows(2);
        // �Բ�ѯ���������а�
        QueryResponse query = solr.query(solrQuery);
        // ���ز�ѯ������(document)
        SolrDocumentList results = query.getResults();
        // ��������
        for (SolrDocument solrDocument : results) {
            System.out.println(solrDocument.getFieldValue("id"));
            System.out.println(solrDocument.getFieldValue("foodName"));
            System.out.println(solrDocument.getFieldValue("price"));
            System.out.println(solrDocument.getFieldValue("typeName"));
        }
        solr.close();
    }

    @Test // ��������ʹ�ù���FilterQueries
    public void readHighlighting() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        // ָ����ѯ����,�÷�����SolrQuery�Ĺ������������Ч����ͬ
        solrQuery.setQuery("foodName:����");
        // ����
        solrQuery.setSort("id", ORDER.desc);
        // ��ҳ:��ʼ������������0��ʼ
        solrQuery.setStart(0);
        // ָ�����ص�document����--������
        solrQuery.setRows(2);
        // ��������
        solrQuery.setHighlight(true);
        // ָ������������
        solrQuery.addHighlightField("foodName");
        // Ч����ͬ---solrQuery.set("hl.fl", "foodName");
        // --solrQuery.set(HighlightParams.FIELDS, "foodName");
        // ���ø�������ʽ
        solrQuery.setHighlightSimplePre("<font color=red>");
        solrQuery.setHighlightSimplePost("</font>");
        // �Բ�ѯ���������а�
        QueryResponse query = solr.query(solrQuery);
        // ���ز�ѯ������(document)
        SolrDocumentList results = query.getResults();
        // ���ظ�������
        Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
        // ��������
        for (SolrDocument solrDocument : results) {
            String id = solrDocument.getFieldValue("id").toString();
            // ����id��ȡ���������ݼ���
            Map<String, List<String>> map = highlighting.get(id);
            List<String> list = map.get("foodName");
            String hlString = list.get(0);
            System.out.println(hlString);
        }
        solr.close();
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
