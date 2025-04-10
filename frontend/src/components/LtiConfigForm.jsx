import React, { useState } from 'react';
import Lti11Form from './Lti11Form';
import Lti13Form from './Lti13Form';

const LtiConfigForm = () => {
    const [activeTab, setActiveTab] = useState('lti11');

    return (
        <div className="lti-config-form">
            <div className="tabs">
                <button
                    type="button"
                    className={activeTab === 'lti11' ? 'active' : ''}
                    onClick={() => setActiveTab('lti11')}
                >
                    LTI 1.1 Configuration
                </button>
                <button
                    type="button"
                    className={activeTab === 'lti13' ? 'active' : ''}
                    onClick={() => setActiveTab('lti13')}
                >
                    LTI 1.3 Configuration
                </button>
            </div>

                {activeTab === 'lti11' ? (
                    <Lti11Form/>
                ) : (
                    <Lti13Form/>
                )}
        </div>
    );
};

export default LtiConfigForm;