-- ==============================================================================
-- SOCH Database Migration (Think. Connect. Grow.)
-- PostgreSQL / Supabase Schema with Row Level Security (RLS) & Triggers
-- ==============================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. PROFILES TABLE
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    username TEXT UNIQUE NOT NULL,
    bio TEXT,
    interests TEXT[] DEFAULT '{}',
    avatar_url TEXT,
    followers_count INTEGER DEFAULT 0,
    following_count INTEGER DEFAULT 0,
    is_online BOOLEAN DEFAULT false,
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3. POSTS TABLE
CREATE TABLE IF NOT EXISTS public.posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL CHECK (char_length(content) <= 500 AND char_length(content) > 0),
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 4. LIKES TABLE
CREATE TABLE IF NOT EXISTS public.likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT unique_post_user_like UNIQUE (post_id, user_id)
);

-- 5. COMMENTS TABLE
CREATE TABLE IF NOT EXISTS public.comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES public.posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL CHECK (char_length(content) > 0),
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 6. FOLLOWS TABLE
CREATE TABLE IF NOT EXISTS public.follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    CONSTRAINT unique_follower_following UNIQUE (follower_id, following_id),
    CONSTRAINT no_self_follow CHECK (follower_id <> following_id)
);

-- 7. CONVERSATIONS & MEMBERS
CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

CREATE TABLE IF NOT EXISTS public.conversation_members (
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    PRIMARY KEY (conversation_id, user_id)
);

-- 8. MESSAGES TABLE
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL CHECK (char_length(content) > 0),
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 9. NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    actor_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    type TEXT NOT NULL CHECK (type IN ('follow', 'like', 'comment', 'message', 'mention')),
    post_id UUID REFERENCES public.posts(id) ON DELETE CASCADE,
    comment_id UUID REFERENCES public.comments(id) ON DELETE CASCADE,
    read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 10. REPORTS TABLE (Moderation)
CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    target_type TEXT NOT NULL CHECK (target_type IN ('post', 'comment', 'profile')),
    target_id UUID NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- ==============================================================================
-- INDEXES FOR PERFORMANCE
-- ==============================================================================
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON public.posts (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON public.posts (user_id);
CREATE INDEX IF NOT EXISTS idx_comments_post_id ON public.comments (post_id);
CREATE INDEX IF NOT EXISTS idx_likes_post_id ON public.likes (post_id);
CREATE INDEX IF NOT EXISTS idx_follows_follower_id ON public.follows (follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following_id ON public.follows (following_id);
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON public.messages (conversation_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON public.notifications (user_id, read);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS)
-- ==============================================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversation_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;

-- Profiles Policies
CREATE POLICY "Public profiles are viewable by everyone" ON public.profiles
    FOR SELECT USING (true);

CREATE POLICY "Users can insert their own profile" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update their own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

-- Posts Policies
CREATE POLICY "Posts are viewable by everyone" ON public.posts
    FOR SELECT USING (true);

CREATE POLICY "Authenticated users can create posts" ON public.posts
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own posts" ON public.posts
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own posts" ON public.posts
    FOR DELETE USING (auth.uid() = user_id);

-- Likes Policies
CREATE POLICY "Likes are viewable by everyone" ON public.likes
    FOR SELECT USING (true);

CREATE POLICY "Users can insert their own likes" ON public.likes
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete their own likes" ON public.likes
    FOR DELETE USING (auth.uid() = user_id);

-- Comments Policies
CREATE POLICY "Comments are viewable by everyone" ON public.comments
    FOR SELECT USING (true);

CREATE POLICY "Authenticated users can add comments" ON public.comments
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete their own comments" ON public.comments
    FOR DELETE USING (auth.uid() = user_id);

-- Follows Policies
CREATE POLICY "Follows are viewable by everyone" ON public.follows
    FOR SELECT USING (true);

CREATE POLICY "Users can create their own follows" ON public.follows
    FOR INSERT WITH CHECK (auth.uid() = follower_id);

CREATE POLICY "Users can delete their own follows" ON public.follows
    FOR DELETE USING (auth.uid() = follower_id);

-- Conversations Policies
CREATE POLICY "Users can view conversations they belong to" ON public.conversations
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.conversation_members
            WHERE conversation_members.conversation_id = conversations.id
            AND conversation_members.user_id = auth.uid()
        )
    );

CREATE POLICY "Authenticated users can start conversations" ON public.conversations
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Members can view conversation members" ON public.conversation_members
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.conversation_members cm
            WHERE cm.conversation_id = conversation_members.conversation_id
            AND cm.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can add conversation members" ON public.conversation_members
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

-- Messages Policies
CREATE POLICY "Users can view messages in their conversations" ON public.messages
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.conversation_members
            WHERE conversation_members.conversation_id = messages.conversation_id
            AND conversation_members.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can insert messages to their conversations" ON public.messages
    FOR INSERT WITH CHECK (
        auth.uid() = sender_id AND
        EXISTS (
            SELECT 1 FROM public.conversation_members
            WHERE conversation_members.conversation_id = messages.conversation_id
            AND conversation_members.user_id = auth.uid()
        )
    );

-- Notifications Policies
CREATE POLICY "Users can view their own notifications" ON public.notifications
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can update their own notifications" ON public.notifications
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can trigger notifications" ON public.notifications
    FOR INSERT WITH CHECK (auth.uid() = actor_id);

-- Reports Policies
CREATE POLICY "Users can insert reports" ON public.reports
    FOR INSERT WITH CHECK (auth.uid() = reporter_id);

-- ==============================================================================
-- DATABASE TRIGGERS FOR COUNTS & PROFILES
-- ==============================================================================

-- Auto create profile on auth signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, name, username, bio, interests, avatar_url)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'name', 'New Member'),
        COALESCE(NEW.raw_user_meta_data->>'username', 'user_' || SUBSTRING(NEW.id::text, 1, 8)),
        'New member on SOCH',
        ARRAY['AI & Tech', 'Productivity'],
        ''
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Maintain likes count
CREATE OR REPLACE FUNCTION public.update_likes_count()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE public.posts SET likes_count = likes_count + 1 WHERE id = NEW.post_id;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE public.posts SET likes_count = GREATEST(0, likes_count - 1) WHERE id = OLD.post_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_like_change
    AFTER INSERT OR DELETE ON public.likes
    FOR EACH ROW EXECUTE FUNCTION public.update_likes_count();

-- Maintain comments count
CREATE OR REPLACE FUNCTION public.update_comments_count()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE public.posts SET comments_count = comments_count + 1 WHERE id = NEW.post_id;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE public.posts SET comments_count = GREATEST(0, comments_count - 1) WHERE id = OLD.post_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_comment_change
    AFTER INSERT OR DELETE ON public.comments
    FOR EACH ROW EXECUTE FUNCTION public.update_comments_count();

-- Maintain follower/following counts
CREATE OR REPLACE FUNCTION public.update_follow_counts()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        UPDATE public.profiles SET followers_count = followers_count + 1 WHERE id = NEW.following_id;
        UPDATE public.profiles SET following_count = following_count + 1 WHERE id = NEW.follower_id;
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE public.profiles SET followers_count = GREATEST(0, followers_count - 1) WHERE id = OLD.following_id;
        UPDATE public.profiles SET following_count = GREATEST(0, following_count - 1) WHERE id = OLD.follower_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER on_follow_change
    AFTER INSERT OR DELETE ON public.follows
    FOR EACH ROW EXECUTE FUNCTION public.update_follow_counts();

-- ==============================================================================
-- SEED DATA: OFFICIAL SOCH CREATOR & DISCOVERY USERS
-- ==============================================================================
INSERT INTO public.profiles (id, name, username, bio, interests, avatar_url, followers_count, following_count, is_verified)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Dipak', 'dipak.dev', 'Building SOCH — a space for ideas, learning, technology and meaningful connections. Exploring AI, building useful products and helping people think, learn and grow.', ARRAY['Developer', 'AI', 'Technology'], 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400', 1000000, 84, true),
    ('00000000-0000-0000-0000-000000000002', 'Aarav', 'aarav.tech', 'Engineering next-gen software systems, open source contributor & building AI apps.', ARRAY['AI & Tech', 'Coding'], 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400', 4200, 150, false),
    ('00000000-0000-0000-0000-000000000003', 'Riya', 'riya.learns', 'Cognitive neuroscience student. Passionate about how our minds perceive knowledge and creativity.', ARRAY['Study', 'Science'], 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400', 8900, 210, false),
    ('00000000-0000-0000-0000-000000000004', 'Kabir', 'kabir.creator', 'Product designer & writer. Exploring minimal interfaces and human-centered thinking.', ARRAY['Creativity', 'Design'], 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400', 12500, 312, false)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    username = EXCLUDED.username,
    bio = EXCLUDED.bio,
    interests = EXCLUDED.interests,
    avatar_url = EXCLUDED.avatar_url,
    followers_count = EXCLUDED.followers_count,
    following_count = EXCLUDED.following_count,
    is_verified = EXCLUDED.is_verified;

-- Seed Initial Posts for Dipak
INSERT INTO public.posts (id, user_id, content, likes_count, comments_count, created_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Welcome to SOCH. A calm space where technology, ideas, and intellect converge. Here we focus on depth, curiosity, and building together. What thought is shaping your day?', 1420, 86, NOW() - INTERVAL '1 hour'),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'The future of software is not more noise—it is elevated signal. With AI as a thinking partner, our creativity is multiplied, not replaced.', 980, 42, NOW() - INTERVAL '4 hours')
ON CONFLICT (id) DO NOTHING;
