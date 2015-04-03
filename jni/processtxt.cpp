#include <jni.h>
#include"processtxt.h"
#include"com_example_tagidentification_ResultsActivity.h"
#include <stdio.h>
#include <string.h>
#include<stdlib.h>
#include<ctype.h>


#define contentNum 48 //所有内容的个数
#define lineNum 50    //表格的行数
#define oneLineNum 3  //每行读取内容的个数
#define TRUE 1
#define FALSE 0


int processFile();
JNIEXPORT jint JNICALL Java_com_example_tagidentification_ResultsActivity_processFileNative
  (JNIEnv *, jobject){
	processFile();
	return 0;
}



/*-----------������---------------*/
//Ӫ��ɷֱ�ṹ��
struct fromContent{
    char *elementsName; //项目
    char *chineseUnit;  //中文单位
    char *englishUnit;  //英文单位
    float precision;    //精度
	float content;      //每100g或者100ml的含量
}fromContent[contentNum] ={
		"能量", "千焦", "kJ", 1.00, 0.00,
			"蛋白质", "克", "g", 0.10, 0.00,
			"脂肪", "克", "g", 0.10, 0.00,
			"饱和脂肪", "克", "g", 0.10, 0.00,
			"饱和脂肪酸", "克", "g", 0.10, 0.00,
			"反式脂肪", "克", "g", 0.10, 0.00,
			"反式脂肪酸", "克", "g", 0.10, 0.00,
			"单不饱和脂肪", "克", "g", 0.10, 0.00,
			"单不饱和脂肪酸", "克", "g", 0.10, 0.00,
			"多不饱和脂肪", "克", "g", 0.10, 0.00,
			"多不饱和脂肪酸", "克", "g", 0.10, 0.00,
			"胆固醇", "毫克", "mg", 1.00, 0.00,
			"碳水化合物", "克", "g", 0.10, 0.00,
			"糖", "克", "g", 0.10, 0.00,
			"乳糖c", "克", "g", 0.10, 0.00,
			"膳食纤维", "克", "g", 0.10, 0.00,
			"单体成分", "克", "g", 0.10, 0.00,
			"可溶性膳食纤维", "克", "g", 0.10, 0.00,
			"不可溶性膳食纤维", "克", "g", 0.10, 0.00,
			"钠", "毫克", "mg", 1.00, 0.00,
			"维生素A", "微克视黄醇当量", "μgRE", 1.00, 0.00,
			"维生素D", "微克", "μg", 0.10, 0.00,
			"维生素E", "毫克α-生育酚当量", "mgα-TE", 0.01, 0.00,
			"维生素K", "微克", "μg", 0.10, 0.00,
			"维生素B2", "毫克", "mg", 0.01, 0.00,
			"核黄素", "毫克", "mg", 0.01, 0.00,
			"维生素B6", "毫克", "mg", 0.01, 0.00,
			"维生素B12", "微克", "μg", 0.01, 0.00,
			"维生素C", "毫克", "mg", 0.10, 0.00,
			"抗坏血酸", "毫克", "mg", 0.10, 0.00,
			"烟酸", "毫克", "mg", 0.01, 0.00,
			"烟酰胺", "毫克", "mg", 0.01, 0.00,
			"叶酸", "微克", "μg", 1.00, 0.00,
			"叶酸", "微克叶酸当量", "μgDFE", 1.00, 0.00,
			"泛酸", "毫克", "mg", 0.01, 0.00,
			"生物素", "微克", "μg", 0.10, 0.00,
			"胆碱", "毫克", "mg", 0.10, 0.00,
			"磷", "毫克", "mg", 1.00, 0.00,
			"钾", "毫克", "mg", 1.00, 0.00,
			"镁", "毫克", "mg", 1.00, 0.00,
			"钙", "毫克", "mg", 1.00, 0.00,
			"铁", "毫克", "mg", 0.10, 0.00,
			"锌", "毫克", "mg", 0.01, 0.00,
			"碘", "微克", "μg", 0.10, 0.00,
			"硒", "微克", "μg", 0.10, 0.00,
			"铜", "毫克", "mg", 0.01, 0.00,
			"氟", "毫克", "mg", 0.01, 0.00,
			"锰", "毫克", "mg", 0.01, 0.00
};

unsigned min(unsigned x,unsigned y,unsigned z );
unsigned lev_distance(const char *s,const char *t);
float getContent(char *c);
void printfFromContent(void);

FILE *fDone; //�����ļ���ָ�룬���ڴ򿪶�ȡ���ļ�

int processFile() {

	char line[lineNum]; //定义一个字符串数组，用于存储读取的字符
	FILE *fFromContent; //定义文件流指针，用于打开读取的文件
	char *content[oneLineNum]; //定义每行中的内容
	char *contentTemp;         //用于临时保存content[oneLineNum]防止null的读入
	unsigned int distance = 0; //两个字符串之间的距离
	char correctContentNum = -1; //距离最小的ContentNum，即与content[0]最相似的fromContent[i].elementsName中的i
	char startContentNum = 0;    //每次循环开始的值，检测到一样的后，上面的不在检测

	char i = 0; //循环变量
	char d = 0; //用于报错调用lev_distance后得到的距离值，防止重复计算
	char energyFlag = FALSE; //是否为能量的标志位


	fDone = fopen("/data/data/com.example.tagidentification/files/result2.txt","w");
    fFromContent = fopen("/data/data/com.example.tagidentification/files/result.txt","r");

    //逐行读取fFromContent所指向文件中的内容
	while (fgets(line,lineNum,fFromContent) != NULL)
	{
		//fprintf(fDone, "aa%s", line);
		//printf("%s", line);

		//将line中空格隔开的内容，分别存入content指针数组中
		contentTemp = strtok(line, " ");
		if(contentTemp)
			content[0] = contentTemp;
		for (i = 1; i < oneLineNum; i++){
			contentTemp = strtok(NULL, " ");
			if(contentTemp)
				content[i] = contentTemp;
		}
		//fprintf(fDone, "bb%s %s\n", content[0], content[1]);
		//fprintf(fDone, "lev_distance = %d\n", lev_distance("Ӫ��", "Ӫ��"));
		//在fromContent中找到与content[0]最相似的
		distance = strlen(content[0]);
		for (i = startContentNum; i < contentNum; i++){  /****Ҫ�޸ı����*****/
			//lev_distance == 0ʱ���ҵ�һ���
			if ((d = lev_distance(content[0], fromContent[i].elementsName)) == 0){
				correctContentNum = i;
				startContentNum = i + 1; //检测两个字符串一样时，将循环起始从下一行开始，不再检测之前的
				//到最后一个时，判断是否为 “能量”，
				if (correctContentNum == 0)
					energyFlag = TRUE;
				break; //若检查到完全一样的立即推出循环
			}
			//否则找到lev_distance值最小的，作为一致的
			else {
				//将距离最小的记录下来
				if (distance > d){
					distance = d;
					correctContentNum = i;
				}
				//到最后一个时，判断是否为 “能量”，
				if ((correctContentNum != -1) && (i == (contentNum-1))){
					if (correctContentNum == 0)
						energyFlag = TRUE;
				}
			}
		}
		//从能量开始输出
		//if ((correctContentNum != -1) && (energyFlag == TRUE)){
		if (correctContentNum != -1){
//			fprintf(fDone, "%s->%s,distance = %d    ", content[0], fromContent[correctContentNum].elementsName, distance);
//			fprintf(fDone, "content = %.2f     ", getContent(content[1]));

			fromContent[correctContentNum].content = getContent(content[1]);
		}


    }
	fclose(fFromContent);

	printfFromContent();
	fclose(fDone);

	return 0;
}

///@func:将content[1]转化为float,即含量的大小
//@paras:c均为c风格字符串
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

//@func:fromConten输出观察
//@paras:无
void printfFromContent(void)
{
	char i = 0;
//	FILE *fDone; //�����ļ���ָ�룬���ڴ򿪶�ȡ���ļ�
//	fDone = fopen("/data/data/com.example.tagidentification/files/result.txt","w"); //ֻ�w�ʽ���ļ�fromContentV1.txt


	for ( ; i < contentNum; i++){
		//printf("%s: %.2f%s(%s)\n", fromContent[i].elementsName, fromContent[i].content, fromContent[i].chineseUnit, fromContent[i].englishUnit);
		fprintf(fDone, "%s: %.2f%s(%s)\n", fromContent[i].elementsName, fromContent[i].content, fromContent[i].chineseUnit, fromContent[i].englishUnit);
	}
//	fclose(fDone);
}


//@func:计算字符串s 和 t之间的levenshtein距离
//@paras:s和t均为c风格字符串
unsigned lev_distance(const char *s,const char *t)
{
	 //n:目标单词t的长度   m:源单词s的长度
    unsigned m_tmp=0,n_tmp=0;
    int i=0;
    //计算源单词长度
    while(s[i])
    {
        i++;
        m_tmp++;
    }
    //计算目标单词长度
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
    //给矩阵的第0行和第0列赋值  ֵ
    for(i=0;i<m;i++)
        matrix[i][0]=i;
    for(i=0;i<n;i++)
        matrix[0][i]=i;
    //填充矩阵的其他元素，逐行填充
    int j;
    for(i=1;i<m;i++)
        for(j=1;j<n;j++)
        {
            unsigned cost=1;
            if(s[i-1]==t[j-1])
                cost=0;
            matrix[i][j]=min(matrix[i-1][j]+1,matrix[i][j-1]+1,matrix[i-1][j-1]+cost);
        }

    return matrix[m-1][n-1];
}

//@func:求三个数的最小值  ֵ
unsigned min(unsigned x,unsigned y,unsigned z )
{
    unsigned tmp=(x<y ? x:y);
    tmp=(tmp<z ? tmp:z);
    return tmp;
}

