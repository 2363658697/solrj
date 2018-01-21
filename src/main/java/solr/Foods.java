package solr;

import org.apache.solr.client.solrj.beans.Field;
/**
 * managed-schema�ļ�������ԶΣ�����foodNameʹ��ik�ִ�
<field name="foodId" type="int" indexed="false" stored="true"/>
<field name="foodName" type="text_ik" indexed="true" stored="true"/>
<field name="price" type="int" indexed="false" stored="true"/>
<field name="typeName" type="string" indexed="true" stored="true"/>

�������ݣ�
       id     foodName    price   foodType
       1        �ཷ����        10      ���
       2      Ϻ�ʳ���         25     ����

 * ͨ������bean�ķ�ʽ��ӵ�������
 * 
 * @author Administrator
 *
 */
public class Foods {
    @Field
    private String id;
    @Field
    private int foodId;
    @Field
    private String foodName;
    @Field
    private int price;
    @Field
    private String typeName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}
