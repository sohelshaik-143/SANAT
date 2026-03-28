import React, { useState, useEffect } from 'react';
import { Users, MessageSquare, Heart, Share2, MapPin, AlertCircle } from 'lucide-react';
import { getComplaints } from '../data/mockData';
import './CitizenDashboard.css';

const Community = () => {
  const [posts, setPosts] = useState([]);

  useEffect(() => {
    // In a real app, this would fetch public reports. For now, we show recent civic issues as community posts.
    const complaints = getComplaints();
    const communityPosts = complaints.map(c => ({
      id: c.id,
      author: 'Citizen Reporter',
      type: c.type,
      content: c.description,
      location: c.location,
      time: c.date,
      likes: Math.floor(Math.random() * 20),
      comments: Math.floor(Math.random() * 5),
    }));
    setPosts(communityPosts);
  }, []);

  return (
    <div className="community-page animate-fade-in px-4 py-6">
      <div className="community-header mb-8">
        <h1 className="text-3xl font-bold flex items-center gap-3">
          <Users size={32} className="text-accent" /> Community Feed
        </h1>
        <p className="text-muted mt-2">See what's happening in your neighborhood and support civic improvements.</p>
      </div>

      <div className="community-grid max-w-4xl mx-auto flex flex-col gap-6">
        {posts.length === 0 ? (
          <div className="glass-panel text-center p-12">
            <MessageSquare size={48} className="mx-auto text-muted mb-4 opacity-30" />
            <h3 className="text-xl font-semibold">No community posts yet</h3>
            <p className="text-muted mt-2">Be the first to share a civic report with the community!</p>
          </div>
        ) : (
          posts.map(post => (
            <div key={post.id} className="glass-panel p-6 hover-glow transition-all duration-300">
              <div className="flex justify-between items-start mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-accent-dim flex items-center justify-center text-accent font-bold">
                    {post.author[0]}
                  </div>
                  <div>
                    <h4 className="font-bold text-sm">{post.author} <span className="text-xs text-muted font-normal ml-2">reported a {post.type}</span></h4>
                    <p className="text-xs text-muted flex items-center gap-1"><MapPin size={10} /> {post.location}</p>
                  </div>
                </div>
                <span className="text-xs text-muted">{post.time}</span>
              </div>

              <p className="text-secondary text-sm leading-relaxed mb-6">
                "{post.content}"
              </p>

              <div className="flex items-center gap-6 pt-4 border-t border-white/5">
                <button className="flex items-center gap-2 text-xs text-muted hover:text-accent transition-colors">
                  <Heart size={16} /> {post.likes} Likes
                </button>
                <button className="flex items-center gap-2 text-xs text-muted hover:text-accent transition-colors">
                  <MessageSquare size={16} /> {post.comments} Comments
                </button>
                <button className="flex items-center gap-2 text-xs text-muted hover:text-accent transition-colors ml-auto">
                  <Share2 size={16} /> Share
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Community Guidelines Sidebar */}
      <div className="community-sidebar-fixed hidden xl:block absolute right-12 top-24 w-64">
        <div className="glass-panel p-4">
          <h3 className="text-sm font-bold mb-3 flex items-center gap-2">
            <AlertCircle size={14} className="text-warning" /> Guidelines
          </h3>
          <ul className="text-xs text-muted flex flex-col gap-2">
            <li>• Keep reports factual and honest.</li>
            <li>• Respect privacy—don't show faces.</li>
            <li>• Use the 'Follow up' feature for updates.</li>
            <li>• Support fellow citizens' reports.</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Community;
