/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.sun.syndication.feed.synd.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Content;
import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Image;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.feed.synd.SyndPerson;

/**
 */
public class ConverterForRSS091Userland extends ConverterForRSS090 {
    public ConverterForRSS091Userland() {
        this("rss_0.91U");
    }

    protected ConverterForRSS091Userland(final String type) {
        super(type);
    }

    @Override
    public void copyInto(final WireFeed feed, final SyndFeed syndFeed) {
        final Channel channel = (Channel) feed;
        super.copyInto(channel, syndFeed);
        syndFeed.setLanguage(channel.getLanguage()); // c
        syndFeed.setCopyright(channel.getCopyright()); // c
        syndFeed.setDocs(channel.getDocs());
        syndFeed.setManagingEditor(channel.getManagingEditor());
        syndFeed.setWebMaster(channel.getWebMaster());
        syndFeed.setGenerator(channel.getGenerator());

        final Date pubDate = channel.getPubDate();

        if (pubDate != null) {
            syndFeed.setPublishedDate(pubDate); // c
        } else if (channel.getLastBuildDate() != null) {
            syndFeed.setPublishedDate(channel.getLastBuildDate()); // c
        }

        final String author = channel.getManagingEditor();

        if (author != null) {
            final List<String> creators = ((DCModule) syndFeed.getModule(DCModule.URI)).getCreators();

            if (!creators.contains(author)) {
                final Set<String> s = new LinkedHashSet<String>(); // using a set to
                // remove
                // duplicates
                s.addAll(creators); // DC creators
                s.add(author); // feed native author
                creators.clear();
                creators.addAll(s);
            }
        }
    }

    protected Description createItemDescription(final SyndContent sContent) {
        final Description desc = new Description();
        desc.setValue(sContent.getValue());
        desc.setType(sContent.getType());

        return desc;
    }

    @Override
    protected Image createRSSImage(final SyndImage sImage) {
        final Image image = super.createRSSImage(sImage);
        image.setDescription(sImage.getDescription());

        return image;
    }

    // for synd -> rss
    // synd.content -> rss.content
    // synd.description -> rss.description
    @Override
    protected Item createRSSItem(final SyndEntry sEntry) {
        final Item item = super.createRSSItem(sEntry);
        item.setComments(sEntry.getComments());
        final SyndContent sContent = sEntry.getDescription();

        if (sContent != null) {
            item.setDescription(createItemDescription(sContent));
        }

        final List<SyndContent> contents = sEntry.getContents();

        if (contents != null && contents.size() > 0) {
            final SyndContent syndContent = contents.get(0);
            final Content cont = new Content();
            cont.setValue(syndContent.getValue());
            cont.setType(syndContent.getType());
            item.setContent(cont);
        }

        return item;
    }

    @Override
    protected WireFeed createRealFeed(final String type, final SyndFeed syndFeed) {
        final Channel channel = (Channel) super.createRealFeed(type, syndFeed);
        channel.setLanguage(syndFeed.getLanguage()); // c
        channel.setCopyright(syndFeed.getCopyright()); // c
        channel.setPubDate(syndFeed.getPublishedDate()); // c
        channel.setDocs(syndFeed.getDocs());
        channel.setManagingEditor(syndFeed.getManagingEditor());
        channel.setWebMaster(syndFeed.getWebMaster());
        channel.setGenerator(syndFeed.getGenerator());

        if (syndFeed.getAuthors() != null && syndFeed.getAuthors().size() > 0) {
            final SyndPerson author = syndFeed.getAuthors().get(0);
            channel.setManagingEditor(author.getName());
        }

        return channel;
    }

    // for rss -> synd
    // rss.content -> synd.content
    // rss.description -> synd.description
    @Override
    protected SyndEntry createSyndEntry(final Item item, final boolean preserveWireItem) {
        final SyndEntry syndEntry = super.createSyndEntry(item, preserveWireItem);
        final Description desc = item.getDescription();
        syndEntry.setComments(item.getComments());

        if (desc != null) {
            final SyndContent descContent = new SyndContentImpl();
            descContent.setType(desc.getType());
            descContent.setValue(desc.getValue());
            syndEntry.setDescription(descContent);
        }

        final Content cont = item.getContent();

        if (cont != null) {
            final SyndContent content = new SyndContentImpl();
            content.setType(cont.getType());
            content.setValue(cont.getValue());

            final List<SyndContent> syndContents = new ArrayList<SyndContent>();
            syndContents.add(content);
            syndEntry.setContents(syndContents);
        }

        return syndEntry;
    }

    @Override
    protected SyndImage createSyndImage(final Image rssImage) {
        final SyndImage syndImage = super.createSyndImage(rssImage);
        syndImage.setDescription(rssImage.getDescription());

        return syndImage;
    }
}
