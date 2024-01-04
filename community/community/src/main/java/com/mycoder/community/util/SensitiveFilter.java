package com.mycoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sun.text.normalizer.Trie;

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

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while((keyword = reader.readLine()) != null){
                // add to Trie
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("load sensitive-words file failure: " + e.getMessage());
        }

    }

    // add a sensitive word to the Trie
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); ++i) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                // init subnode
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            tempNode = subNode;

            // set end flag
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * filter sensitive word
     * @param text original text
     * @return text after filter
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // pointer 1: in Trie
        TrieNode tempNode = rootNode;
        // pointer 2: for starter in the text
        int begin = 0;
        // pointer 3: for end in the text
        int position = 0;
        // result string
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // skip symbol
            if (isSymbol(c)) {
                // if pointer1 is at the root
                if (tempNode == rootNode) {
                    sb.append(c);
                    ++begin;
                }
                // no matter symbol is at the begin or in the middle,
                ++position;

                continue;
            }

            // check subNode
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // the text start with bigin is not a sensitive word
                sb.append(text.charAt(begin));

                ++begin;
                position = begin;
                // pointer3 repoint to the root node
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd) {
                // find a sensitive word, replace the part from begin to position
                sb.append(REPLACEMENT);

                ++position;
                begin = position;
                tempNode = rootNode;
            } else {
                // check next character
                ++position;
            }

        }

        // put the last character(s) to the result
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // whether it is symbol or not
    private boolean isSymbol(Character c) {
        return !CharUtils.isAsciiAlphanumeric(c) && (c <0x2E80 || c > 0x9FFF); // 0x2E80 - 0x9FFF 东亚文字范围
    }

    // 前缀树
    private class TrieNode {

        // keyword end identify
        private boolean isKeywordEnd = false;

        // key是下级字符 value是下级节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

}
