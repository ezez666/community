package com.nowcoder.community.util;


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    private TrieNode rootNode = new TrieNode();
    private static final String REPLACEMENT = "***";
    @PostConstruct
    public void init(){
        try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ){
            String str = "";
            while ((str = reader.readLine())!=null){
                this.addSensitive(str);
            }
        }catch (IOException e){
            logger.error("初始化敏感词失败"+e.getMessage());
        }
    }

    public void addSensitive(String keyWords){
        TrieNode tempNode = rootNode;
        for(int i=0;i<keyWords.length();i++){
            char c = keyWords.charAt(i);
            if (tempNode.getSubNodes(c)==null){
                TrieNode subNode = new TrieNode();
                tempNode.setSubNodes(c,subNode);
            }
            tempNode = tempNode.getSubNodes(c);
            if(i == keyWords.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();
        while (position < text.length()){
            char c = text.charAt(position);
            if(isSymbol(c)){
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getSubNodes(c);
            if(tempNode == null){
                tempNode = rootNode;
                sb.append(text.charAt(begin));
                position = ++begin;
            }else if(tempNode.isKeywordEnd()){
                sb.append(REPLACEMENT);
                tempNode = rootNode;
                begin=++position;
            }else {
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
    private class TrieNode{
        private boolean isKeywordEnd;
        Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void setSubNodes(Character c,TrieNode subNode){
            subNodes.put(c,subNode);
        }
        public TrieNode getSubNodes(Character c){
            return subNodes.get(c);
        }
    }
}
