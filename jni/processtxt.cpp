#include <jni.h>
#include"processtxt.h"
#include"com_example_tagidentification_ResultsActivity.h"
#include <stdio.h>
#include <string.h>
#include<stdlib.h>
#include<ctype.h>


#define contentNum 48 //�������ݵĸ���
#define lineNum 50    //��������
#define oneLineNum 3  //ÿ�ж�ȡ���ݵĸ���
#define TRUE 1
#define FALSE 0


int processFile();
JNIEXPORT jint JNICALL Java_com_example_tagidentification_ResultsActivity_processFileNative
  (JNIEnv *, jobject){
	processFile();
	return 0;
}



/*-----------��������---------------*/
//Ӫ���ɷֱ�ṹ��
struct fromContent{
    char *elementsName; //��Ŀ
    char *chineseUnit;  //���ĵ�λ
    char *englishUnit;  //Ӣ�ĵ�λ
    float precision;    //����
	float content;      //ÿ100g����100ml�ĺ���
}fromContent[contentNum] ={
	"����", "ǧ��", "kJ", 1.00, 0.00,
	"������", "��", "g", 0.10, 0.00,
	"֬��", "��", "g", 0.10, 0.00,
	"����֬��", "��", "g", 0.10, 0.00,
	"����֬����", "��", "g", 0.10, 0.00,
	"��ʽ֬��", "��", "g", 0.10, 0.00,
	"��ʽ֬����", "��", "g", 0.10, 0.00,
	"��������֬��", "��", "g", 0.10, 0.00,
	"��������֬����", "��", "g", 0.10, 0.00,
	"�಻����֬��", "��", "g", 0.10, 0.00,
	"�಻����֬����", "��", "g", 0.10, 0.00,
	"���̴�", "����", "mg", 1.00, 0.00,
	"̼ˮ������", "��", "g", 0.10, 0.00,
	"��", "��", "g", 0.10, 0.00,
	"����c", "��", "g", 0.10, 0.00,
	"��ʳ��ά", "��", "g", 0.10, 0.00,
	"����ɷ�", "��", "g", 0.10, 0.00,
	"��������ʳ��ά", "��", "g", 0.10, 0.00,
	"����������ʳ��ά", "��", "g", 0.10, 0.00,
	"��", "����", "mg", 1.00, 0.00,
	"ά����A", "΢���ӻƴ�����", "��gRE", 1.00, 0.00,
	"ά����D", "΢��", "��g", 0.10, 0.00,
	"ά����E", "���˦�-�����ӵ���", "mg��-TE", 0.01, 0.00,
	"ά����K", "΢��", "��g", 0.10, 0.00,
	"ά����B2", "����", "mg", 0.01, 0.00,
	"�˻���", "����", "mg", 0.01, 0.00,
	"ά����B6", "����", "mg", 0.01, 0.00,
	"ά����B12", "΢��", "��g", 0.01, 0.00,
	"ά����C", "����", "mg", 0.10, 0.00,
	"����Ѫ��", "����", "mg", 0.10, 0.00,
	"����", "����", "mg", 0.01, 0.00,
	"������", "����", "mg", 0.01, 0.00,
	"Ҷ��", "΢��", "��g", 1.00, 0.00,
	"Ҷ��", "΢��Ҷ�ᵱ��", "��gDFE", 1.00, 0.00,
	"����", "����", "mg", 0.01, 0.00,
	"������", "΢��", "��g", 0.10, 0.00,
	"����", "����", "mg", 0.10, 0.00,
	"��", "����", "mg", 1.00, 0.00,
	"��", "����", "mg", 1.00, 0.00,
	"þ", "����", "mg", 1.00, 0.00,
	"��", "����", "mg", 1.00, 0.00,
	"��", "����", "mg", 0.10, 0.00,
	"п", "����", "mg", 0.01, 0.00,
	"��", "΢��", "��g", 0.10, 0.00,
	"��", "΢��", "��g", 0.10, 0.00,
	"ͭ", "����", "mg", 0.01, 0.00,
	"��", "����", "mg", 0.01, 0.00,
	"��", "����", "mg", 0.01, 0.00
};

unsigned min(unsigned x,unsigned y,unsigned z );
unsigned lev_distance(const char *s,const char *t);
float getContent(char *c);
void printfFromContent(void);

FILE *fDone; //�����ļ���ָ�룬���ڴ򿪶�ȡ���ļ�

int processFile() {

	char line[lineNum]; //����һ���ַ������飬���ڴ洢��ȡ���ַ�
	FILE *fFromContent; //�����ļ���ָ�룬���ڴ򿪶�ȡ���ļ�
	char *content[oneLineNum]; //����ÿ���е�����
	char *contentTemp;         //������ʱ����content[oneLineNum]��ֹnull�Ķ���
	unsigned int distance = 0; //�����ַ���֮��ľ���
	char correctContentNum = -1; //������С��ContentNum������content[0]�����Ƶ�fromContent[i].elementsName�е�i
	char startContentNum = 0;    //ÿ��ѭ����ʼ��ֵ����⵽һ���ĺ�����Ĳ��ڼ��

	char i = 0; //ѭ������
	char d = 0; //���ڱ������lev_distance��õ��ľ���ֵ����ֹ�ظ�����
	char energyFlag = FALSE; //�Ƿ�Ϊ�����ı�־λ


	fDone = fopen("/data/data/com.example.tagidentification/files/result2.txt","w"); //ֻ����ʽ���ļ�fromContentV1.txt
    fFromContent = fopen("/data/data/com.example.tagidentification/files/result.txt","r"); //ֻ����ʽ���ļ�fromContentV1.txt

	//���ж�ȡfFromContent��ָ���ļ��е�����
	while (fgets(line,lineNum,fFromContent) != NULL)
	{
		fprintf(fDone, "aa%s", line);
		//printf("%s", line);

		//��line�пո���������ݣ��ֱ����contentָ��������
		contentTemp = strtok(line, " ");
		if(contentTemp)
			content[0] = contentTemp;
		for (i = 1; i < oneLineNum; i++){
			contentTemp = strtok(NULL, " ");
			if(contentTemp)
				content[i] = contentTemp;
		}
		fprintf(fDone, "bb%s %s\n", content[0], content[1]);
		//fprintf(fDone, "lev_distance = %d\n", lev_distance("Ӫ��", "Ӫ��"));
		//��fromContent���ҵ���content[0]�����Ƶ�
		distance = strlen(content[0]);
		for (i = startContentNum; i < contentNum; i++){  /****Ҫ�޸ı�����*****/
			//lev_distance == 0ʱ���ҵ�һ����
			if ((d = lev_distance(content[0], fromContent[i].elementsName)) == 0){
				correctContentNum = i;
				startContentNum = i + 1; //��������ַ���һ��ʱ����ѭ����ʼ����һ�п�ʼ�����ټ��֮ǰ��
				//�����һ��ʱ���ж��Ƿ�Ϊ ����������
				if (correctContentNum == 0)
					energyFlag = TRUE;
				break; //����鵽��ȫһ���������Ƴ�ѭ��
			}
			//�����ҵ�lev_distanceֵ��С�ģ���Ϊһ�µ�
			else {
				//��������С�ļ�¼����
				if (distance > d){
					distance = d;
					correctContentNum = i;
				}
				//�����һ��ʱ���ж��Ƿ�Ϊ ����������
				if ((correctContentNum != -1) && (i == (contentNum-1))){
					if (correctContentNum == 0)
						energyFlag = TRUE;
				}
			}
		}
		//��������ʼ���
		//if ((correctContentNum != -1) && (energyFlag == TRUE)){
		if (correctContentNum != -1){
			fprintf(fDone, "%s->%s,distance = %d\n", content[0], fromContent[correctContentNum].elementsName, distance);
			fprintf(fDone, "%.2f\n", getContent(content[1]));

			fromContent[correctContentNum].content = getContent(content[1]);
		}


    }
	fclose(fFromContent);


	printfFromContent();
	fclose(fDone);
	return 0;
}

//@func:��content[1]ת��Ϊfloat,�������Ĵ�С
//@paras:c��Ϊc����ַ���
float getContent(char *c)
{
	float content = 0;
	float power = 1;

	while (isdigit(*c)){
		content = content * 10 + (*c - '0');
		c++;
	}
	if (*c == '.'){
		c++;
	}
	while (isdigit(*c)){
		power *= 10;
		content = content * 10 + (*c - '0');
		c++;
	}
	content /= power;
	//printf("%.2f\n", content);

	return content;
}

//@func:fromConten����۲�
//@paras:��
void printfFromContent(void)
{
	char i = 0;
//	FILE *fDone; //�����ļ���ָ�룬���ڴ򿪶�ȡ���ļ�
//	fDone = fopen("/data/data/com.example.tagidentification/files/result.txt","w"); //ֻ����ʽ���ļ�fromContentV1.txt


	for ( ; i < contentNum; i++){
		//printf("%s: %.2f%s(%s)\n", fromContent[i].elementsName, fromContent[i].content, fromContent[i].chineseUnit, fromContent[i].englishUnit);
		fprintf(fDone, "%s: %.2f%s(%s)\n", fromContent[i].elementsName, fromContent[i].content, fromContent[i].chineseUnit, fromContent[i].englishUnit);
	}
//	fclose(fDone);
}


//@func:�����ַ���s �� t֮���levenshtein����
//@paras:s��t��Ϊc����ַ���
unsigned lev_distance(const char *s,const char *t)
{
    //n:Ŀ�굥��t�ĳ���   m:Դ����s�ĳ���
    unsigned m_tmp=0,n_tmp=0;
    int i=0;
    //����Դ���ʳ���
    while(s[i])
    {
        i++;
        m_tmp++;
    }
    //����Ŀ�굥�ʳ���
    i=0;
    while(t[i])
    {
        i++;
        n_tmp++;
    }
    if(m_tmp==0)
        return n_tmp;
    if(n_tmp==0)
        return m_tmp;

    const unsigned m=m_tmp+1;
    const unsigned n=n_tmp+1;
    unsigned matrix[m][n];
    //������ĵ�0�к͵�0�и�ֵ
    for(i=0;i<m;i++)
        matrix[i][0]=i;
    for(i=0;i<n;i++)
        matrix[0][i]=i;
    //�����������Ԫ�أ��������
    int j;
    for(i=1;i<m;i++)
        for(j=1;j<n;j++)
        {
            unsigned cost=1;
            if(s[i-1]==t[j-1])
                cost=0;
            matrix[i][j]=min(matrix[i-1][j]+1,matrix[i][j-1]+1,matrix[i-1][j-1]+cost);
        }
    //�鿴�����Ԫ�ص�ֵ
//	for(i=0;i<m;i++)
//    {
//        for(j=0;j<n;j++)
//        {
//            printf("%d\t",matrix[i][j]);
//        }
//        printf("\n");
//    }
    //����matrix[m-1][n-1],�������ַ���֮��ľ���
    return matrix[m-1][n-1];
}

//@func:������������Сֵ
unsigned min(unsigned x,unsigned y,unsigned z )
{
    unsigned tmp=(x<y ? x:y);
    tmp=(tmp<z ? tmp:z);
    return tmp;
}

